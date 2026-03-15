package com.backend.checkout_service.checkout.service;

import com.backend.checkout_service.cart.domain.Cart;
import com.backend.checkout_service.cart.domain.CartStatus;
import com.backend.checkout_service.cart.exception.CartNotFoundException;
import com.backend.checkout_service.cart.exception.InvalidCartStateException;
import com.backend.checkout_service.cart.repository.CartRepository;
import com.backend.checkout_service.checkout.dto.CheckoutResponse;
import com.backend.checkout_service.pricing.dto.PricingResult;
import com.backend.checkout_service.pricing.service.PricingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CheckoutServiceImpl implements CheckoutService {

    private static final Logger log = LoggerFactory.getLogger(CheckoutServiceImpl.class);
    private final CartRepository cartRepository;
    private final PricingService pricingService;

    public CheckoutServiceImpl(CartRepository cartRepository, PricingService pricingService) {
        this.cartRepository = cartRepository;
        this.pricingService = pricingService;
    }

    @Override
    @Transactional
    public CheckoutResponse checkout(UUID cartId) {
        Cart cart = fetchCart(cartId);

        validateCartIsOpen(cart);
        PricingResult pricingResult = pricingService.calculate(cart.getItems());

        return new CheckoutResponse(
                cart.getId(),
                "EUR",
                pricingResult.items().stream()
                        .map(item -> new CheckoutResponse.CheckoutItem(
                                item.productCode(),
                                item.productName(),
                                item.unit(),
                                item.unitPrice(),
                                item.quantity(),
                                item.lineSubtotal()
                        ))
                        .toList(),
                pricingResult.subtotal(),
                pricingResult.items().stream()
                        .flatMap(item -> item.discounts().stream())
                        .map(discount -> new CheckoutResponse.AppliedDiscountItem(
                                discount.productCode(),
                                discount.description(),
                                discount.amount()
                        ))
                        .toList(),
                pricingResult.totalDiscount(),
                pricingResult.total()
        );
    }

    private Cart fetchCart(UUID cartId) {
        return cartRepository.findByIdWithItems(cartId)
                .orElseThrow(() -> {
                    log.info("Cart not found with id {}", cartId);
                    return new CartNotFoundException("cart not found");
                });
    }

    private void validateCartIsOpen(Cart cart) {
        if (cart.getStatus() != CartStatus.OPEN) {
            log.warn("Cannot modify cart {} because status is {}", cart.getId(), cart.getStatus());
            throw new InvalidCartStateException("Cart is not open");
        }
    }

}
