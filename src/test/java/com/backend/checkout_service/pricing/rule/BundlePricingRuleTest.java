package com.backend.checkout_service.pricing.rule;

import com.backend.checkout_service.cart.domain.CartItem;
import com.backend.checkout_service.offer.domain.Offer;
import com.backend.checkout_service.pricing.dto.PricingItemResult;
import com.backend.checkout_service.product.domain.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BundlePricingRuleTest {

    private final BundlePricingRule bundlePricingRule = new BundlePricingRule();

    @Test
    @DisplayName("should support calculation when offer is present")
    void shouldSupportCalculationWhenOfferIsPresent() {
        Offer offer = createOffer(createAppleProduct(), 2, "0.45");

        assertTrue(bundlePricingRule.supports(offer));
    }

    @Test
    @DisplayName("should not support calculation when offer is null")
    void shouldNotSupportCalculationWhenOfferIsNull() {
        assertFalse(bundlePricingRule.supports(null));
    }

    @Test
    @DisplayName("should calculate bundled price with remainder correctly")
    void shouldCalculateBundledPriceWithRemainderCorrectly() {
        Product apple = createAppleProduct();
        CartItem item = createCartItem(apple, 5);
        Offer offer = createOffer(apple, 2, "0.45");

        PricingItemResult result = bundlePricingRule.calculate(item, offer);

        assertEquals("APPLE", result.productCode());
        assertEquals("Apple", result.productName());
        assertEquals("piece", result.unit());
        assertEquals(new BigDecimal("0.30"), result.unitPrice());
        assertEquals(5, result.quantity());

        // regular subtotal = 5 * 0.30 = 1.50
        assertEquals(new BigDecimal("1.50"), result.lineSubtotal());

        // offer: 2 for 0.45 -> two bundles = 0.90, remainder 1 = 0.30
        // final total = 1.20
        assertEquals(new BigDecimal("1.20"), result.lineTotal());

        assertEquals(1, result.discounts().size());
        assertEquals("APPLE", result.discounts().get(0).productCode());
        assertEquals("2 for €0,45", result.discounts().get(0).description());
        assertEquals(new BigDecimal("0.30"), result.discounts().get(0).amount());
    }

    @Test
    @DisplayName("should calculate exact bundle correctly")
    void shouldCalculateExactBundleCorrectly() {
        Product apple = createAppleProduct();
        CartItem item = createCartItem(apple, 4);
        Offer offer = createOffer(apple, 2, "0.45");

        PricingItemResult result = bundlePricingRule.calculate(item, offer);

        // regular subtotal = 4 * 0.30 = 1.20
        assertEquals(new BigDecimal("1.20"), result.lineSubtotal());

        // two bundles = 0.90
        assertEquals(new BigDecimal("0.90"), result.lineTotal());

        // discount = 0.30
        assertEquals(new BigDecimal("0.30"), result.discounts().get(0).amount());
    }

    @Test
    @DisplayName("should calculate single bundle correctly")
    void shouldCalculateSingleBundleCorrectly() {
        Product apple = createAppleProduct();
        CartItem item = createCartItem(apple, 2);
        Offer offer = createOffer(apple, 2, "0.45");

        PricingItemResult result = bundlePricingRule.calculate(item, offer);

        assertEquals(new BigDecimal("0.60"), result.lineSubtotal());
        assertEquals(new BigDecimal("0.45"), result.lineTotal());
        assertEquals(new BigDecimal("0.15"), result.discounts().get(0).amount());
    }

    @Test
    @DisplayName("should return no discount when offer gives no savings")
    void shouldReturnNoDiscountWhenOfferGivesNoSavings() {
        Product apple = createAppleProduct();
        CartItem item = createCartItem(apple, 2);

        // same as regular price: 2 * 0.30 = 0.60
        Offer offer = createOffer(apple, 2, "0.60");

        PricingItemResult result = bundlePricingRule.calculate(item, offer);

        assertEquals(new BigDecimal("0.60"), result.lineSubtotal());
        assertEquals(new BigDecimal("0.60"), result.lineTotal());
        assertTrue(result.discounts().isEmpty());
    }

    private Product createAppleProduct() {
        return new Product(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                "APPLE",
                "Apple",
                "piece",
                new BigDecimal("0.30")
        );
    }

    private Offer createOffer(Product product, int quantity, String bundlePrice) {
        return new Offer(
                UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
                product,
                quantity,
                new BigDecimal(bundlePrice),
                Instant.parse("2026-03-10T00:00:00Z"),
                Instant.parse("2026-03-17T23:59:59Z"),
                Instant.parse("2026-03-10T00:00:00Z")
        );
    }

    private CartItem createCartItem(Product product, int quantity) {
        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(quantity);
        return item;
    }
}