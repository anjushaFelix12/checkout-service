package com.backend.checkout_service.checkout.controller;

import com.backend.checkout_service.cart.dto.CartResponse;
import com.backend.checkout_service.checkout.dto.CheckoutResponse;
import com.backend.checkout_service.checkout.service.CheckoutService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Checkout", description = "Checkout APIs")
@RestController
@RequestMapping("api/v1/carts")
public class CheckoutController {

    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }
    @GetMapping("/{cartId}/checkout")
    public ResponseEntity<CheckoutResponse> getCart(@PathVariable UUID cartId) {
        CheckoutResponse response =  checkoutService.checkout(cartId);
        return ResponseEntity.ok(response);
    }
}
