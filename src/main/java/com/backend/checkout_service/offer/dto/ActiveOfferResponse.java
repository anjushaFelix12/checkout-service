package com.backend.checkout_service.offer.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ActiveOfferResponse(String productCode, Integer quantity, BigDecimal bundlePrice, String description, Instant validUntil) {}
