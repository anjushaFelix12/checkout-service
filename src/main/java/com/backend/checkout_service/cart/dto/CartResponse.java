package com.backend.checkout_service.cart.dto;

import com.backend.checkout_service.cart.domain.CartStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CartResponse(UUID id, CartStatus status, List<ProductItem> items, Integer itemCount, Integer totalItems,
                           BigDecimal subtotal) {
}
