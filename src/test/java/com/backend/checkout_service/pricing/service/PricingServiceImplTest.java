package com.backend.checkout_service.pricing.service;

import com.backend.checkout_service.cart.domain.CartItem;
import com.backend.checkout_service.offer.domain.Offer;
import com.backend.checkout_service.offer.repository.OfferRepository;
import com.backend.checkout_service.pricing.dto.PricingResult;
import com.backend.checkout_service.pricing.rule.BundlePricingRule;
import com.backend.checkout_service.pricing.rule.PricingRule;
import com.backend.checkout_service.product.domain.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PricingServiceImplTest {

    @Mock
    private OfferRepository offerRepository;

    private PricingServiceImpl pricingService;

    private Product apple;
    private Product banana;
    private Offer appleOffer;

    @BeforeEach
    void setUp() {
        PricingRule bundlePricingRule = new BundlePricingRule();
        pricingService = new PricingServiceImpl(offerRepository, List.of(bundlePricingRule));

        apple = new Product(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                "APPLE",
                "Apple",
                "piece",
                new BigDecimal("0.30")
        );

        banana = new Product(
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                "BANANA",
                "Banana",
                "piece",
                new BigDecimal("0.50")
        );

        appleOffer = new Offer(
                UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
                apple,
                2,
                new BigDecimal("0.45"),
                Instant.parse("2026-03-10T00:00:00Z"),
                Instant.parse("2026-03-17T23:59:59Z"),
                Instant.parse("2026-03-10T00:00:00Z")
        );
    }

    @Test
    @DisplayName("should calculate total with active offer and regular pricing")
    void shouldCalculateTotalWithActiveOfferAndRegularPricing() {
        CartItem appleItem = createCartItem(apple, 3);
        CartItem bananaItem = createCartItem(banana, 1);

        when(offerRepository.findByProductIdInAndValidUntilAfter(any(), any()))
                .thenReturn(List.of(appleOffer));

        PricingResult result = pricingService.calculate(List.of(appleItem, bananaItem));

        assertEquals(2, result.items().size());

        // subtotal = apple(0.90) + banana(0.50) = 1.40
        assertEquals(new BigDecimal("1.40"), result.subtotal());

        // discount = apple only -> 0.15
        assertEquals(new BigDecimal("0.15"), result.totalDiscount());

        // total = 1.25
        assertEquals(new BigDecimal("1.25"), result.total());
    }

    @Test
    @DisplayName("should calculate regular pricing when no offer exists")
    void shouldCalculateRegularPricingWhenNoOfferExists() {
        CartItem bananaItem = createCartItem(banana, 2);

        when(offerRepository.findByProductIdInAndValidUntilAfter(any(), any()))
                .thenReturn(List.of());

        PricingResult result = pricingService.calculate(List.of(bananaItem));

        assertEquals(1, result.items().size());
        assertEquals(new BigDecimal("1.00"), result.subtotal());
        assertEquals(BigDecimal.ZERO, result.totalDiscount());
        assertEquals(new BigDecimal("1.00"), result.total());
        assertTrue(result.items().get(0).discounts().isEmpty());
    }

    @Test
    @DisplayName("should calculate multiple bundle applications correctly")
    void shouldCalculateMultipleBundleApplicationsCorrectly() {
        CartItem appleItem = createCartItem(apple, 5);

        when(offerRepository.findByProductIdInAndValidUntilAfter(any(), any()))
                .thenReturn(List.of(appleOffer));

        PricingResult result = pricingService.calculate(List.of(appleItem));

        // regular subtotal = 5 * 0.30 = 1.50
        assertEquals(new BigDecimal("1.50"), result.subtotal());

        // offer applied twice + remainder = 1.20
        assertEquals(new BigDecimal("1.20"), result.total());

        // discount = 0.30
        assertEquals(new BigDecimal("0.30"), result.totalDiscount());
    }

    @Test
    @DisplayName("should return zero totals for empty cart")
    void shouldReturnZeroTotalsForEmptyCart() {
        when(offerRepository.findByProductIdInAndValidUntilAfter(any(), any()))
                .thenReturn(List.of());

        PricingResult result = pricingService.calculate(List.of());

        assertNotNull(result);
        assertTrue(result.items().isEmpty());
        assertEquals(BigDecimal.ZERO, result.subtotal());
        assertEquals(BigDecimal.ZERO, result.totalDiscount());
        assertEquals(BigDecimal.ZERO, result.total());
    }

    @Test
    @DisplayName("should calculate mixed cart with one discounted and one non discounted item")
    void shouldCalculateMixedCartWithOneDiscountedAndOneNonDiscountedItem() {
        CartItem appleItem = createCartItem(apple, 2);
        CartItem bananaItem = createCartItem(banana, 2);

        when(offerRepository.findByProductIdInAndValidUntilAfter(any(), any()))
                .thenReturn(List.of(appleOffer));

        PricingResult result = pricingService.calculate(List.of(appleItem, bananaItem));

        // subtotal = apple(0.60) + banana(1.00) = 1.60
        assertEquals(new BigDecimal("1.60"), result.subtotal());

        // discount = apple only = 0.15
        assertEquals(new BigDecimal("0.15"), result.totalDiscount());

        // total = 1.45
        assertEquals(new BigDecimal("1.45"), result.total());
    }

    private CartItem createCartItem(Product product, int quantity) {
        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(quantity);
        return item;
    }
}