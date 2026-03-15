package com.backend.checkout_service.checkout.service;

import com.backend.checkout_service.cart.domain.Cart;
import com.backend.checkout_service.cart.domain.CartItem;
import com.backend.checkout_service.cart.domain.CartStatus;
import com.backend.checkout_service.cart.exception.CartNotFoundException;
import com.backend.checkout_service.cart.exception.InvalidCartStateException;
import com.backend.checkout_service.cart.repository.CartRepository;
import com.backend.checkout_service.checkout.dto.CheckoutResponse;
import com.backend.checkout_service.pricing.dto.AppliedDiscount;
import com.backend.checkout_service.pricing.dto.PricingItemResult;
import com.backend.checkout_service.pricing.dto.PricingResult;
import com.backend.checkout_service.pricing.service.PricingService;
import com.backend.checkout_service.product.domain.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private PricingService pricingService;

    @InjectMocks
    private CheckoutServiceImpl checkoutService;

    private UUID cartId;
    private Cart cart;
    private CartItem appleItem;
    private Product apple;

    @BeforeEach
    void setUp() {
        cartId = UUID.fromString("99999999-9999-9999-9999-999999999999");

        apple = new Product(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                "APPLE",
                "Apple",
                "piece",
                new BigDecimal("0.30")
        );

        appleItem = new CartItem();
        appleItem.setProduct(apple);
        appleItem.setQuantity(3);

        cart = new Cart();
        cart.setId(cartId);
        cart.setStatus(CartStatus.OPEN);
        cart.setItems(List.of(appleItem));
    }

    @Test
    @DisplayName("should checkout successfully when cart is open and not empty")
    void shouldCheckoutSuccessfullyWhenCartIsOpenAndNotEmpty() {
        PricingItemResult itemResult = new PricingItemResult(
                "APPLE",
                "Apple",
                "piece",
                new BigDecimal("0.30"),
                3,
                new BigDecimal("0.90"),
                List.of(new AppliedDiscount("APPLE", "2 for €0.45", new BigDecimal("0.15"))),
                new BigDecimal("0.75")
        );

        PricingResult pricingResult = new PricingResult(
                List.of(itemResult),
                new BigDecimal("0.90"),
                new BigDecimal("0.15"),
                new BigDecimal("0.75")
        );

        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));
        when(pricingService.calculate(cart.getItems())).thenReturn(pricingResult);

        CheckoutResponse response = checkoutService.checkout(cartId);

        assertNotNull(response);
        assertEquals(cartId, response.cartId());
        assertEquals("EUR", response.currency());
        assertEquals(new BigDecimal("0.90"), response.subtotal());
        assertEquals(new BigDecimal("0.15"), response.totalDiscount());
        assertEquals(new BigDecimal("0.75"), response.total());

        assertEquals(1, response.items().size());
        assertEquals("APPLE", response.items().get(0).productCode());
        assertEquals("Apple", response.items().get(0).productName());
        assertEquals("piece", response.items().get(0).unit());
        assertEquals(new BigDecimal("0.30"), response.items().get(0).unitPrice());
        assertEquals(3, response.items().get(0).quantity());
        assertEquals(new BigDecimal("0.90"), response.items().get(0).lineSubtotal());

        assertEquals(1, response.discounts().size());
        assertEquals("APPLE", response.discounts().get(0).productCode());
        assertEquals("2 for €0.45", response.discounts().get(0).description());
        assertEquals(new BigDecimal("0.15"), response.discounts().get(0).amount());

        verify(cartRepository).findById(cartId);
        verify(pricingService).calculate(cart.getItems());
        verifyNoMoreInteractions(cartRepository, pricingService);
    }

    @Test
    @DisplayName("should throw when cart is not found")
    void shouldThrowWhenCartIsNotFound() {
        when(cartRepository.findById(cartId)).thenReturn(Optional.empty());

        assertThrows(CartNotFoundException.class, () -> checkoutService.checkout(cartId));

        verify(cartRepository).findById(cartId);
        verifyNoMoreInteractions(cartRepository, pricingService);
    }

    @Test
    @DisplayName("should throw when cart status is not OPEN")
    void shouldThrowWhenCartStatusIsNotOpen() {
        cart.setStatus(CartStatus.CHECKED_OUT);

        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));

        InvalidCartStateException ex = assertThrows(
                InvalidCartStateException.class,
                () -> checkoutService.checkout(cartId)
        );

        assertEquals("Cart is not open", ex.getMessage());

        verify(cartRepository).findById(cartId);
        verifyNoMoreInteractions(cartRepository, pricingService);
    }
}