package com.backend.checkout_service.pricing.service;

import com.backend.checkout_service.cart.domain.CartItem;
import com.backend.checkout_service.pricing.dto.PricingResult;

import java.util.List;

public interface PricingService {

    PricingResult calculate(List<CartItem> cartItems);
}
