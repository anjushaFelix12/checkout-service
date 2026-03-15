package com.backend.checkout_service.cart.controller;

import com.backend.checkout_service.cart.domain.CartStatus;
import com.backend.checkout_service.cart.dto.CartResponse;
import com.backend.checkout_service.cart.dto.ProductItem;
import com.backend.checkout_service.cart.exception.CartNotFoundException;
import com.backend.checkout_service.cart.exception.InvalidCartItemException;
import com.backend.checkout_service.cart.exception.InvalidCartStateException;
import com.backend.checkout_service.cart.service.CartService;
import com.backend.checkout_service.product.exception.InvalidProductException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CartController.class)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CartService cartService;

    @Test
    void createCart_shouldReturnCreatedCart() throws Exception {
        UUID cartId = UUID.randomUUID();

        CartResponse response = new CartResponse(
                cartId,
                CartStatus.OPEN,
                List.of(),
                0,
                0,
                BigDecimal.ZERO
        );

        when(cartService.createCart()).thenReturn(response);

        mockMvc.perform(post("/api/v1/carts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cartId.toString()))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.itemCount").value(0))
                .andExpect(jsonPath("$.totalItems").value(0));
    }

    @Nested
    class GetAllCarts {
        @Test
        void getCart_shouldReturnCart() throws Exception {
            UUID cartId = UUID.randomUUID();

            CartResponse response = new CartResponse(
                    cartId,
                    CartStatus.OPEN,
                    List.of(
                            new ProductItem("APPLE", "Apple", "piece", new BigDecimal("0.30"), 3, new BigDecimal("0.90"))
                    ),
                    1,
                    3,
                    new BigDecimal("0.90")
            );

            when(cartService.getCart(cartId)).thenReturn(response);

            mockMvc.perform(get("/api/v1/carts/{cartId}", cartId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(cartId.toString()))
                    .andExpect(jsonPath("$.items[0].productCode").value("APPLE"))
                    .andExpect(jsonPath("$.subtotal").value(0.90));
        }

        @Test
        void getCart_shouldReturn404WhenNotFound() throws Exception {
            UUID cartId = UUID.randomUUID();

            when(cartService.getCart(cartId)).thenThrow(new CartNotFoundException("CartNotFoundException"));

            mockMvc.perform(get("/api/v1/carts/{cartId}", cartId))
                    .andExpect(status().isNotFound());
        }

        @Test
        void getCart_shouldReturn400WhenUuidInvalid() throws Exception {
            mockMvc.perform(get("/api/v1/carts/{cartId}", "bad-uuid"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class AddCartItem {
        @Test
        void addItem_shouldReturnUpdatedCart() throws Exception {
            UUID cartId = UUID.randomUUID();

            String requestBody = """
                    {
                      "productCode": "APPLE",
                      "quantity": 3
                    }
                    """;

            CartResponse response = new CartResponse(
                    cartId,
                    CartStatus.OPEN,
                    List.of(new ProductItem("APPLE", "Apple", "piece", new BigDecimal("0.30"), 3, new BigDecimal("0.90"))),
                    1,
                    3,
                    new BigDecimal("0.90")
            );

            when(cartService.addItem(eq(cartId), org.mockito.ArgumentMatchers.any())).thenReturn(response);

            mockMvc.perform(post("/api/v1/carts/{cartId}/items", cartId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.itemCount").value(1))
                    .andExpect(jsonPath("$.totalItems").value(3))
                    .andExpect(jsonPath("$.items[0].productCode").value("APPLE"));
        }

        @Test
        void addItem_shouldReturn404WhenCartNotFound() throws Exception {
            UUID cartId = UUID.randomUUID();

            String requestBody = """
                    {
                      "productCode": "APPLE",
                      "quantity": 3
                    }
                    """;

            when(cartService.addItem(eq(cartId), org.mockito.ArgumentMatchers.any()))
                    .thenThrow(new CartNotFoundException("CartNotFoundException"));

            mockMvc.perform(post("/api/v1/carts/{cartId}/items", cartId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isNotFound());
        }

        @Test
        void addItem_shouldReturn404WhenProductNotFound() throws Exception {
            UUID cartId = UUID.randomUUID();

            String requestBody = """
                    {
                      "productCode": "APPLE",
                      "quantity": 3
                    }
                    """;

            when(cartService.addItem(eq(cartId), org.mockito.ArgumentMatchers.any()))
                    .thenThrow(new InvalidProductException("Product not found"));

            mockMvc.perform(post("/api/v1/carts/{cartId}/items", cartId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void addItem_shouldReturn409WhenCartNotOpen() throws Exception {
            UUID cartId = UUID.randomUUID();

            String requestBody = """
                    {
                      "productCode": "APPLE",
                      "quantity": 3
                    }
                    """;

            when(cartService.addItem(eq(cartId), org.mockito.ArgumentMatchers.any()))
                    .thenThrow(new InvalidCartStateException("Cart is not open"));

            mockMvc.perform(post("/api/v1/carts/{cartId}/items", cartId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class UpdateItem {
        @Test
        void patchItem_shouldReturnUpdatedCart() throws Exception {
            UUID cartId = UUID.randomUUID();

            String requestBody = """
                    {
                      "quantity": 5
                    }
                    """;

            CartResponse response = new CartResponse(
                    cartId,
                    CartStatus.OPEN,
                    List.of(new ProductItem("APPLE", "Apple", "piece", new BigDecimal("0.30"), 5, new BigDecimal("1.50"))),
                    1,
                    5,
                    new BigDecimal("1.50")
            );

            when(cartService.updateItem(eq(cartId), eq("APPLE"), org.mockito.ArgumentMatchers.any()))
                    .thenReturn(response);

            mockMvc.perform(patch("/api/v1/carts/{cartId}/items/{productCode}", cartId, "APPLE")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalItems").value(5))
                    .andExpect(jsonPath("$.subtotal").value(1.50));
        }

        @Test
        void patchItem_shouldReturn404WhenItemNotFound() throws Exception {
            UUID cartId = UUID.randomUUID();

            String requestBody = """
                    {
                      "quantity": 5
                    }
                    """;

            when(cartService.updateItem(eq(cartId), eq("APPLE"), org.mockito.ArgumentMatchers.any()))
                    .thenThrow(new InvalidCartItemException("Item not found"));

            mockMvc.perform(patch("/api/v1/carts/{cartId}/items/{productCode}", cartId, "APPLE")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class RemoveItem {
        @Test
        void removeItem_shouldReturn204() throws Exception {
            UUID cartId = UUID.randomUUID();

            mockMvc.perform(delete("/api/v1/carts/{cartId}/items/{productCode}", cartId, "APPLE"))
                    .andExpect(status().isNoContent());
        }

        @Test
        void removeItem_shouldReturn404WhenCartNotFound() throws Exception {
            UUID cartId = UUID.randomUUID();

            doThrow(new CartNotFoundException("CartNotFoundException"))
                    .when(cartService).removeItem(cartId, "APPLE");

            mockMvc.perform(delete("/api/v1/carts/{cartId}/items/{productCode}", cartId, "APPLE"))
                    .andExpect(status().isNotFound());
        }

        @Test
        void removeItem_shouldReturn409WhenCartNotOpen() throws Exception {
            UUID cartId = UUID.randomUUID();

            doThrow(new InvalidCartStateException("Cart is not open"))
                    .when(cartService).removeItem(cartId, "APPLE");

            mockMvc.perform(delete("/api/v1/carts/{cartId}/items/{productCode}", cartId, "APPLE"))
                    .andExpect(status().isBadRequest());
        }
    }
}