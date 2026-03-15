package com.backend.checkout_service.pricing.dto;

import java.math.BigDecimal;
import java.util.List;

public record PricingItemResult(
        String productCode,
        String productName,
        String unit,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal lineSubtotal,
        List<AppliedDiscount> discounts,
        BigDecimal lineTotal
) {
}