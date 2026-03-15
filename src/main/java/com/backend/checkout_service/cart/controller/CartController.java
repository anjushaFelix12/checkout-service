package com.backend.checkout_service.cart.controller;

import com.backend.checkout_service.cart.dto.AddCartItemRequest;
import com.backend.checkout_service.cart.dto.CartResponse;
import com.backend.checkout_service.cart.dto.UpdateCartItemRequest;
import com.backend.checkout_service.cart.service.CartService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Cart", description = "Cart APIs")
@RestController
@RequestMapping("api/v1/carts")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping
    public CartResponse createCart() {
        return cartService.createCart();
    }

    @GetMapping("/{cartId}")
    public CartResponse getCart(@PathVariable UUID cartId) {
        return cartService.getCart(cartId);
    }

    @PostMapping("/{cartId}/items")
    public CartResponse addItem(@PathVariable UUID cartId,
                                @RequestBody AddCartItemRequest request) {
        return cartService.addItem(cartId, request);
    }

    @PatchMapping("/{cartId}/items/{productCode}")
    public CartResponse updateItem(
            @PathVariable UUID cartId,
            @PathVariable String productCode,
            @Valid @RequestBody UpdateCartItemRequest request) {

        return cartService.updateItem(cartId, productCode, request.quantity());
    }

    @DeleteMapping("/{cartId}/items/{productCode}")
    public ResponseEntity<Void> removeItem(
            @PathVariable UUID cartId,
            @PathVariable String productCode) {

        cartService.removeItem(cartId, productCode);

        return ResponseEntity.noContent().build();
    }
}
