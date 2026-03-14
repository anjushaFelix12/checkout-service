package com.backend.checkout_service.product.dto;

import java.math.BigDecimal;

public record ActiveOffer(Integer quantity, BigDecimal bundlePrice, String description) {}

