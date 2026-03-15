package com.backend.checkout_service.product.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductResponse(UUID id, String code, String name, String unit, BigDecimal unitPrice,
                              ActiveOffer activeOffer) {
}

