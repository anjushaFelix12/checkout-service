package com.backend.checkout_service.cart.dto;

import java.math.BigDecimal;

public record ProductItem(String productCode, String productName, String unit, BigDecimal unitPrice, Integer quantity,
                          BigDecimal lineTotal) {
}
