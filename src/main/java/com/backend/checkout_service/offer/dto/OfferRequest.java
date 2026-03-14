package com.backend.checkout_service.offer.dto;

import java.math.BigDecimal;

public record OfferRequest(String productCode, Integer quantity, BigDecimal bundlePrice) {
}
