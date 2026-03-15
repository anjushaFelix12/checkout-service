package com.backend.checkout_service.pricing.rule;

import com.backend.checkout_service.cart.domain.CartItem;
import com.backend.checkout_service.offer.domain.Offer;
import com.backend.checkout_service.pricing.dto.AppliedDiscount;
import com.backend.checkout_service.pricing.dto.PricingItemResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class BundlePricingRule implements PricingRule {

    @Override
    public boolean supports(Offer offer) {
        return offer != null;
    }

    @Override
    public PricingItemResult calculate(CartItem item, Offer offer) {
        int quantity = item.getQuantity();
        int offerQuantity = offer.getQuantity();

        int bundleCount = quantity / offerQuantity;
        int remainder = quantity % offerQuantity;

        BigDecimal unitPrice = item.getProduct().getUnitPrice();
        BigDecimal regularSubtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));

        BigDecimal bundleTotal = offer.getBundlePrice()
                .multiply(BigDecimal.valueOf(bundleCount));

        BigDecimal remainderTotal = unitPrice
                .multiply(BigDecimal.valueOf(remainder));

        BigDecimal finalLineTotal = bundleTotal.add(remainderTotal);
        BigDecimal discountAmount = regularSubtotal.subtract(finalLineTotal);

        List<AppliedDiscount> discounts = discountAmount.compareTo(BigDecimal.ZERO) > 0
                ? List.of(new AppliedDiscount(
                item.getProduct().getCode(),
                String.format("%d for €%.2f", offer.getQuantity(), offer.getBundlePrice()),
                discountAmount
        ))
                : List.of();

        return new PricingItemResult(
                item.getProduct().getCode(),
                item.getProduct().getName(),
                item.getProduct().getUnit(),
                unitPrice,
                quantity,
                regularSubtotal,
                discounts,
                finalLineTotal
        );
    }
}