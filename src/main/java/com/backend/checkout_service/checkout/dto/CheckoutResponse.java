package com.backend.checkout_service.checkout.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CheckoutResponse(
        UUID cartId,
        String currency,
        List<CheckoutItem> items,
        BigDecimal subtotal,
        List<AppliedDiscountItem> discounts,
        BigDecimal totalDiscount,
        BigDecimal total
) {

    public record CheckoutItem(
            String productCode,
            String productName,
            String unit,
            BigDecimal unitPrice,
            Integer quantity,
            BigDecimal lineSubtotal
    ) {}

    public record AppliedDiscountItem(
            String productCode,
            String description,
            BigDecimal amount
    ) {}
}