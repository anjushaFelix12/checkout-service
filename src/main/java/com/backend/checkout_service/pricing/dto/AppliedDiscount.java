package com.backend.checkout_service.pricing.dto;

import java.math.BigDecimal;

public record AppliedDiscount(
        String productCode,
        String description,
        BigDecimal amount
) {
}