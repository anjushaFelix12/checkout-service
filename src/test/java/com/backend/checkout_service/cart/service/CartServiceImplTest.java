package com.backend.checkout_service.cart.service;

import com.backend.checkout_service.cart.domain.Cart;
import com.backend.checkout_service.cart.domain.CartItem;
import com.backend.checkout_service.cart.domain.CartStatus;
import com.backend.checkout_service.cart.dto.CartResponse;
import com.backend.checkout_service.cart.exception.CartItemNotFoundException;
import com.backend.checkout_service.cart.exception.CartNotFoundException;
import com.backend.checkout_service.cart.exception.InvalidCartItemException;
import com.backend.checkout_service.cart.exception.InvalidCartStateException;
import com.backend.checkout_service.cart.repository.CartItemRepository;
import com.backend.checkout_service.cart.repository.CartRepository;
import com.backend.checkout_service.product.domain.Product;
import com.backend.checkout_service.product.exception.ProductNotFoundException;
import com.backend.checkout_service.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    private UUID cartId;
    private UUID productId;
    private Cart cart;
    private Product apple;

    @BeforeEach
    void setUp() {
        cartId = UUID.randomUUID();
        productId = UUID.randomUUID();

        cart = new Cart();
        cart.setId(cartId);
        cart.setStatus(CartStatus.OPEN);
        cart.setCreatedAt(Instant.now());
        cart.setUpdatedAt(Instant.now());

        apple = new Product();
        apple.setId(productId);
        apple.setCode("APPLE");
        apple.setName("Apple");
        apple.setUnit("piece");
        apple.setUnitPrice(new BigDecimal("0.30"));
    }

    @Test
    void createCart_shouldCreateEmptyCart() {
        Cart saved = new Cart();
        saved.setId(cartId);
        saved.setStatus(CartStatus.OPEN);
        saved.setCreatedAt(Instant.now());
        saved.setUpdatedAt(Instant.now());

        when(cartRepository.save(any(Cart.class))).thenReturn(saved);

        CartResponse response = cartService.createCart();

        assertThat(response.id()).isEqualTo(cartId);
        assertThat(response.status()).isEqualTo(CartStatus.OPEN);
        assertThat(response.items()).isEmpty();
        assertThat(response.itemCount()).isEqualTo(0);
        assertThat(response.totalItems()).isEqualTo(0);
        assertThat(response.subtotal()).isEqualByComparingTo(BigDecimal.ZERO);

        verify(cartRepository).save(any(Cart.class));
    }

    @Nested
    class AddCartItemRequest {
        @Test
        void addItem_shouldAddNewItem() {
            com.backend.checkout_service.cart.dto.AddCartItemRequest request = new com.backend.checkout_service.cart.dto.AddCartItemRequest("APPLE", 3);

            when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));
            when(productRepository.findByCode("APPLE")).thenReturn(Optional.of(apple));
            when(cartItemRepository.findByCartIdAndProductId(cartId, productId)).thenReturn(Optional.empty());

            CartItem savedItem = new CartItem();
            savedItem.setCart(cart);
            savedItem.setProduct(apple);
            savedItem.setQuantity(3);

            when(cartItemRepository.save(any(CartItem.class))).thenReturn(savedItem);
            when(cartRepository.save(any(Cart.class))).thenReturn(cart);
            when(cartItemRepository.findAllByCartIdWithProduct(cartId)).thenReturn(List.of(savedItem));

            CartResponse response = cartService.addItem(cartId, request);

            assertThat(response.itemCount()).isEqualTo(1);
            assertThat(response.totalItems()).isEqualTo(3);
            assertThat(response.subtotal()).isEqualByComparingTo("0.90");

            verify(cartItemRepository).save(any(CartItem.class));
            verify(cartRepository).save(cart);
        }

        @Test
        void addItem_shouldMergeQuantityWhenItemAlreadyExists() {
            com.backend.checkout_service.cart.dto.AddCartItemRequest request = new com.backend.checkout_service.cart.dto.AddCartItemRequest("APPLE", 3);

            CartItem existingItem = new CartItem();
            existingItem.setCart(cart);
            existingItem.setProduct(apple);
            existingItem.setQuantity(2);

            when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));
            when(productRepository.findByCode("APPLE")).thenReturn(Optional.of(apple));
            when(cartItemRepository.findByCartIdAndProductId(cartId, productId)).thenReturn(Optional.of(existingItem));
            when(cartItemRepository.save(existingItem)).thenReturn(existingItem);
            when(cartRepository.save(cart)).thenReturn(cart);
            when(cartItemRepository.findAllByCartIdWithProduct(cartId)).thenReturn(List.of(existingItem));

            CartResponse response = cartService.addItem(cartId, request);

            assertThat(existingItem.getQuantity()).isEqualTo(5);
            assertThat(response.totalItems()).isEqualTo(5);
            assertThat(response.subtotal()).isEqualByComparingTo("1.50");
        }

        @Test
        void addItem_shouldThrowWhenCartNotOpen() {
            cart.setStatus(CartStatus.CHECKED_OUT);

            com.backend.checkout_service.cart.dto.AddCartItemRequest request = new com.backend.checkout_service.cart.dto.AddCartItemRequest("APPLE", 1);

            when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));

            assertThatThrownBy(() -> cartService.addItem(cartId, request))
                    .isInstanceOf(InvalidCartStateException.class);
        }

        @Test
        void addItem_shouldThrowWhenProductNotFound() {
            com.backend.checkout_service.cart.dto.AddCartItemRequest request = new com.backend.checkout_service.cart.dto.AddCartItemRequest("APPLE", 1);

            when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));
            when(productRepository.findByCode("APPLE")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cartService.addItem(cartId, request))
                    .isInstanceOf(ProductNotFoundException.class);
        }

        @Test
        void addItem_shouldThrowWhenCartNotFound() {
            com.backend.checkout_service.cart.dto.AddCartItemRequest request = new com.backend.checkout_service.cart.dto.AddCartItemRequest("APPLE", 1);

            when(cartRepository.findById(cartId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cartService.addItem(cartId, request))
                    .isInstanceOf(CartNotFoundException.class);
        }
    }

    @Nested
    class UpdateItemRequest {
        @Test
        void updateItem_shouldUpdateQuantity() {

            CartItem existingItem = new CartItem();
            existingItem.setCart(cart);
            existingItem.setProduct(apple);
            existingItem.setQuantity(2);

            when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));
            when(productRepository.findByCode("APPLE")).thenReturn(Optional.of(apple));
            when(cartItemRepository.findByCartIdAndProductId(cartId, productId)).thenReturn(Optional.of(existingItem));
            when(cartItemRepository.save(existingItem)).thenReturn(existingItem);
            when(cartRepository.save(cart)).thenReturn(cart);
            when(cartItemRepository.findAllByCartIdWithProduct(cartId)).thenReturn(List.of(existingItem));

            CartResponse response = cartService.updateItem(cartId, "APPLE", 5);

            assertThat(existingItem.getQuantity()).isEqualTo(7);
            assertThat(response.totalItems()).isEqualTo(7);
            assertThat(response.subtotal()).isEqualByComparingTo("2.10");
        }

        @Test
        void updateItem_shouldThrowWhenItemNotFound() {
            when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));
            when(productRepository.findByCode("APPLE")).thenReturn(Optional.of(apple));
            when(cartItemRepository.findByCartIdAndProductId(cartId, productId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cartService.updateItem(cartId, "APPLE", 5))
                    .isInstanceOf(CartItemNotFoundException.class);
        }
    }

    @Nested
    class DeleteItem {
        @Test
        void removeItem_shouldDeleteItem() {
            CartItem existingItem = new CartItem();
            existingItem.setCart(cart);
            existingItem.setProduct(apple);
            existingItem.setQuantity(2);

            when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));
            when(productRepository.findByCode("APPLE")).thenReturn(Optional.of(apple));
            when(cartItemRepository.findByCartIdAndProductId(cartId, productId)).thenReturn(Optional.of(existingItem));
            when(cartRepository.save(cart)).thenReturn(cart);

            cartService.removeItem(cartId, "APPLE");

            verify(cartItemRepository).delete(existingItem);
            verify(cartRepository).save(cart);
        }

        @Test
        void removeItem_shouldThrowWhenItemNotFound() {
            when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));
            when(productRepository.findByCode("APPLE")).thenReturn(Optional.of(apple));
            when(cartItemRepository.findByCartIdAndProductId(cartId, productId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cartService.removeItem(cartId, "APPLE"))
                    .isInstanceOf(CartItemNotFoundException.class);
        }

        @Test
        void removeItem_shouldThrowWhenCartNotOpen() {
            cart.setStatus(CartStatus.CHECKED_OUT);

            when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));

            assertThatThrownBy(() -> cartService.removeItem(cartId, "APPLE"))
                    .isInstanceOf(InvalidCartStateException.class);
        }
    }

    //    @Test
//    void createCart_shouldCreateEmptyCart() {
//        Cart saved = new Cart();
//        saved.setId(cartId);
//        saved.setStatus(CartStatus.OPEN);
//        saved.setCreatedAt(Instant.now());
//        saved.setUpdatedAt(Instant.now());
//
//        when(cartRepository.save(any(Cart.class))).thenReturn(saved);
//
//        CartResponse response = cartService.createCart();
//
//        assertThat(response.id()).isEqualTo(cartId);
//        assertThat(response.status()).isEqualTo(CartStatus.OPEN);
//        assertThat(response.items()).isEmpty();
//        assertThat(response.itemCount()).isEqualTo(0);
//        assertThat(response.totalItems()).isEqualTo(0);
//        assertThat(response.subtotal()).isEqualByComparingTo(BigDecimal.ZERO);
//
//        verify(cartRepository).save(any(Cart.class));
//    }
    @Nested
    class GetCartsTests {
        @Test
        void getCart_shouldReturnEmptyCart() {
            when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));
            when(cartItemRepository.findAllByCartIdWithProduct(cartId)).thenReturn(List.of());

            CartResponse response = cartService.getCart(cartId);

            assertThat(response.id()).isEqualTo(cartId);
            assertThat(response.items()).isEmpty();
            assertThat(response.itemCount()).isEqualTo(0);
            assertThat(response.totalItems()).isEqualTo(0);
            assertThat(response.subtotal()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        void getCart_shouldReturnCartWithItems() {
            CartItem item = new CartItem();
            item.setCart(cart);
            item.setProduct(apple);
            item.setQuantity(3);

            when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));
            when(cartItemRepository.findAllByCartIdWithProduct(cartId)).thenReturn(List.of(item));

            CartResponse response = cartService.getCart(cartId);

            assertThat(response.itemCount()).isEqualTo(1);
            assertThat(response.totalItems()).isEqualTo(3);
            assertThat(response.subtotal()).isEqualByComparingTo("0.90");
            assertThat(response.items()).hasSize(1);
            assertThat(response.items().get(0).productCode()).isEqualTo("APPLE");
            assertThat(response.items().get(0).lineTotal()).isEqualByComparingTo("0.90");
        }

        @Test
        void getCart_shouldThrowWhenCartIdIsNull() {
            assertThatThrownBy(() -> cartService.getCart(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cartId");
        }

        @Test
        void getCart_shouldThrowWhenCartNotFound() {
            when(cartRepository.findById(cartId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cartService.getCart(cartId))
                    .isInstanceOf(CartNotFoundException.class);
        }
    }
}