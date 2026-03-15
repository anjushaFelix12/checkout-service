package com.backend.checkout_service.checkout.service;

import com.backend.checkout_service.checkout.dto.CheckoutResponse;

import java.util.UUID;

public interface CheckoutService {
    CheckoutResponse checkout(UUID cartId);
}
