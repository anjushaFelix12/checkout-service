package com.backend.checkout_service.cart.service;

import com.backend.checkout_service.cart.dto.AddCartItemRequest;
import com.backend.checkout_service.cart.dto.CartResponse;

import java.util.UUID;

public interface CartService {

    CartResponse createCart();

    CartResponse getCart(UUID cartId);

    CartResponse addItem(UUID cartId, AddCartItemRequest request);

    CartResponse updateItem(UUID cartId, String productCode, Integer quantity);

    void removeItem(UUID cartId, String productCode);
}
