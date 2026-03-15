package com.backend.checkout_service.pricing.rule;

import com.backend.checkout_service.cart.domain.CartItem;
import com.backend.checkout_service.offer.domain.Offer;
import com.backend.checkout_service.pricing.dto.PricingItemResult;

public interface PricingRule {

    boolean supports(Offer offer);

    PricingItemResult calculate(CartItem item, Offer offer);
}