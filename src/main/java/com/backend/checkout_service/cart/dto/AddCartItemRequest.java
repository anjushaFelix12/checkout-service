package com.backend.checkout_service.cart.dto;

public record AddCartItemRequest(String productCode, Integer quantity) {
}
