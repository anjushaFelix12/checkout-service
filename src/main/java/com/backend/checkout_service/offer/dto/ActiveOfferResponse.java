package com.backend.checkout_service.offer.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ActiveOfferResponse(UUID id, String productCode, Integer quantity, BigDecimal bundlePrice,
                                  String description, Instant validUntil) {
}
