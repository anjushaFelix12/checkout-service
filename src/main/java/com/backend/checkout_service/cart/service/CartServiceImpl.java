package com.backend.checkout_service.cart.service;

import com.backend.checkout_service.cart.domain.Cart;
import com.backend.checkout_service.cart.domain.CartItem;
import com.backend.checkout_service.cart.domain.CartStatus;
import com.backend.checkout_service.cart.dto.AddCartItemRequest;
import com.backend.checkout_service.cart.dto.CartResponse;
import com.backend.checkout_service.cart.dto.ProductItem;
import com.backend.checkout_service.cart.exception.CartItemNotFoundException;
import com.backend.checkout_service.cart.exception.CartNotFoundException;
import com.backend.checkout_service.cart.exception.InvalidCartItemException;
import com.backend.checkout_service.cart.exception.InvalidCartStateException;
import com.backend.checkout_service.cart.repository.CartItemRepository;
import com.backend.checkout_service.cart.repository.CartRepository;
import com.backend.checkout_service.product.domain.Product;
import com.backend.checkout_service.product.exception.ProductNotFoundException;
import com.backend.checkout_service.product.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class CartServiceImpl implements CartService {

    private static final Logger log = LoggerFactory.getLogger(CartServiceImpl.class);
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;


    public CartServiceImpl(CartRepository cartRepository,
                           CartItemRepository cartItemRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public CartResponse createCart() {

        log.info("Creating new cart");

        Cart cart = new Cart();
        cart.setStatus(CartStatus.OPEN);
        cart.setCreatedAt(Instant.now());
        cart.setUpdatedAt(Instant.now());

        Cart savedCart = cartRepository.save(cart);

        log.info("Cart created with id={}", savedCart.getId());

        return new CartResponse(
                savedCart.getId(),
                savedCart.getStatus(),
                List.of(),
                0,
                0,
                BigDecimal.ZERO
        );
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart(UUID cartId) {
        if (cartId == null) {
            throw new IllegalArgumentException("cartId must not be null");
        }

        Cart cart = fetchCart(cartId);
        return buildCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse addItem(UUID cartId, AddCartItemRequest request) {

        validateAddItemRequest(cartId, request);

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> {
                    log.warn("Cart not found: {}", cartId);
                    return new CartNotFoundException("Cart not found");
                });

        validateCartIsOpen(cart);

        String normalizedProductCode = normalizeProductCode(request.productCode());

        Product product = fetchProduct(normalizedProductCode);

        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cartId, product.getId())
                .map(existingItem -> {
                    int newQuantity = existingItem.getQuantity() + request.quantity();

                    if (newQuantity <= 0) {
                        log.warn("Calculated quantity became invalid for cart={}, productCode={}, quantity={}",
                                cartId, normalizedProductCode, newQuantity);
                        throw new InvalidCartItemException("Resulting quantity must be greater than 0");
                    }

                    existingItem.setQuantity(newQuantity);
                    return existingItem;
                })
                .orElseGet(() -> {
                    CartItem newItem = new CartItem();
                    newItem.setCart(cart);
                    newItem.setProduct(product);
                    newItem.setQuantity(request.quantity());
                    return newItem;
                });

        cartItemRepository.save(cartItem);

        cart.setUpdatedAt(Instant.now());
        cartRepository.save(cart);

        return buildCartResponse(cart);
    }

    private Cart fetchCart(UUID cartId) {
        return cartRepository.findById(cartId)
                .orElseThrow(() -> {
                    log.info("Cart not found with id {}", cartId);
                    return new CartNotFoundException("cart not found");
                });
    }

    @Override
    @Transactional
    public CartResponse updateItem(UUID cartId, String productCode, Integer quantity) {

        validateUpdateItemRequest(cartId, productCode, quantity);

        Cart cart = fetchCart(cartId);

        validateCartIsOpen(cart);

        String normalizedProductCode = normalizeProductCode(productCode);

        Product product = fetchProduct(normalizedProductCode);

        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cartId, product.getId())
                .orElseThrow(() -> {
                    log.warn("Cart item not found. cartId={}, productCode={}", cartId, normalizedProductCode);
                    return new CartItemNotFoundException(
                            "Item not found in cart for product: " + normalizedProductCode
                    );
                });

        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);

        cart.setUpdatedAt(Instant.now());
        cartRepository.save(cart);

        return buildCartResponse(cart);
    }

    @Override
    @Transactional
    public void removeItem(UUID cartId, String productCode) {

        validateRemoveItemRequest(cartId, productCode);

        Cart cart = fetchCart(cartId);

        validateCartIsOpen(cart);

        String normalizedProductCode = normalizeProductCode(productCode);

        Product product = fetchProduct(normalizedProductCode);

        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cartId, product.getId())
                .orElseThrow(() -> {
                    log.warn("Cart item not found for removal. cartId={}, productCode={}",
                            cartId, normalizedProductCode);
                    return new CartItemNotFoundException(
                            "Item not found in cart for product: " + normalizedProductCode
                    );
                });

        cartItemRepository.delete(cartItem);

        cart.setUpdatedAt(Instant.now());
        cartRepository.save(cart);
    }

    private ProductItem toProductItem(CartItem cartItem) {
        BigDecimal unitPrice = cartItem.getProduct().getUnitPrice();
        int quantity = cartItem.getQuantity();

        BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));

        return new ProductItem(
                cartItem.getProduct().getCode(),
                cartItem.getProduct().getName(),
                cartItem.getProduct().getUnit(),
                unitPrice,
                quantity,
                lineTotal
        );
    }

    private void validateAddItemRequest(UUID cartId, AddCartItemRequest request) {
        if (cartId == null) {
            throw new IllegalArgumentException("cartId must not be null");
        }

        if (request == null) {
            throw new IllegalArgumentException("Request body must not be null");
        }

        if (request.productCode() == null || request.productCode().trim().isEmpty()) {
            throw new IllegalArgumentException("productCode must not be blank");
        }

        if (request.quantity() == null) {
            throw new IllegalArgumentException("quantity must not be null");
        }

        if (request.quantity() <= 0) {
            throw new IllegalArgumentException("quantity must be greater than 0");
        }
    }

    private void validateUpdateItemRequest(UUID cartId, String productCode, Integer quantity) {
        if (cartId == null) {
            throw new IllegalArgumentException("cartId must not be null");
        }

        if (productCode == null || productCode.trim().isEmpty()) {
            throw new IllegalArgumentException("productCode must not be blank");
        }


        if (quantity == null) {
            throw new IllegalArgumentException("quantity must not be null");
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be greater than 0");
        }
    }

    private void validateRemoveItemRequest(UUID cartId, String productCode) {
        if (cartId == null) {
            throw new IllegalArgumentException("cartId must not be null");
        }

        if (productCode == null || productCode.trim().isEmpty()) {
            throw new IllegalArgumentException("productCode must not be blank");
        }
    }

    private void validateCartIsOpen(Cart cart) {
        if (cart.getStatus() != CartStatus.OPEN) {
            log.warn("Cannot modify cart {} because status is {}", cart.getId(), cart.getStatus());
            throw new InvalidCartStateException("Cart is not open");
        }
    }

    private String normalizeProductCode(String productCode) {
        return productCode.trim().toUpperCase();
    }

    private CartResponse buildCartResponse(Cart cart) {
        List<CartItem> cartItems = cartItemRepository.findAllByCartIdWithProduct(cart.getId());

        List<ProductItem> items = cartItems.stream()
                .sorted(Comparator.comparing(ci -> ci.getProduct().getCode()))
                .map(this::toProductItem)
                .toList();

        int itemCount = items.size();
        int totalItems = items.stream().map(ProductItem::quantity).mapToInt(Integer::intValue).sum();
        BigDecimal subtotal = items.stream()
                .map(ProductItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(
                cart.getId(),
                cart.getStatus(),
                items,
                itemCount,
                totalItems,
                subtotal
        );
    }

    private Product fetchProduct(String productCode) {
        return productRepository.findByCode(productCode)
                .orElseThrow(() -> {
                    log.warn("Product not found for code={}", productCode);
                    return new ProductNotFoundException("Product not found: " + productCode);
                });
    }

}
