package com.backend.checkout_service.pricing.dto;

import java.math.BigDecimal;
import java.util.List;

public record PricingResult(
        List<PricingItemResult> items,
        BigDecimal subtotal,
        BigDecimal totalDiscount,
        BigDecimal total
) {
}