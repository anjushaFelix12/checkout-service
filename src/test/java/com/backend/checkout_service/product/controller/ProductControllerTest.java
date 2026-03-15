package com.backend.checkout_service.product.controller;

import com.backend.checkout_service.product.dto.ProductResponse;
import com.backend.checkout_service.product.service.ProductService;
import org.junit.jupiter.api.DisplayName;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Test
    @DisplayName("should return paginated products with active offer")
    void shouldReturnPaginatedProductsWithActiveOffer() throws Exception {
        UUID appleId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID bananaId = UUID.fromString("22222222-2222-2222-2222-222222222222");

        List<ProductResponse> products = List.of(
                new ProductResponse(
                        appleId,
                        "APPLE",
                        "Apple",
                        "piece",
                        new BigDecimal("0.30"),
                        new ProductResponse.ActiveOffer(2, new BigDecimal("0.45"), "2 for €0.45")
                ),
                new ProductResponse(
                        bananaId,
                        "BANANA",
                        "Banana",
                        "piece",
                        new BigDecimal("0.50"),
                        null
                )
        );

        when(productService.getAllProducts(eq(0), eq(10))).thenReturn(products);

        mockMvc.perform(get("/api/v1/products")
                        .param("offset", "0")
                        .param("limit", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))

                .andExpect(jsonPath("$.length()").value(2))

                .andExpect(jsonPath("$[0].id").value(appleId.toString()))
                .andExpect(jsonPath("$[0].code").value("APPLE"))
                .andExpect(jsonPath("$[0].name").value("Apple"))
                .andExpect(jsonPath("$[0].unit").value("piece"))
                .andExpect(jsonPath("$[0].unitPrice").value(0.30))
                .andExpect(jsonPath("$[0].activeOffer.quantity").value(2))
                .andExpect(jsonPath("$[0].activeOffer.bundlePrice").value(0.45))
                .andExpect(jsonPath("$[0].activeOffer.description").value("2 for €0.45"))

                .andExpect(jsonPath("$[1].id").value(bananaId.toString()))
                .andExpect(jsonPath("$[1].code").value("BANANA"))
                .andExpect(jsonPath("$[1].name").value("Banana"))
                .andExpect(jsonPath("$[1].unit").value("piece"))
                .andExpect(jsonPath("$[1].unitPrice").value(0.50))
                .andExpect(jsonPath("$[1].activeOffer").doesNotExist());

        verify(productService).getAllProducts(0, 10);
    }

    @Test
    @DisplayName("should use default pagination values when params are not provided")
    void shouldUseDefaultPaginationValues() throws Exception {
        when(productService.getAllProducts(eq(0), eq(10))).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/products")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(productService).getAllProducts(0, 10);
    }

    @Test
    @DisplayName("should return empty list when no products exist")
    void shouldReturnEmptyListWhenNoProductsExist() throws Exception {
        when(productService.getAllProducts(eq(0), eq(5))).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/products")
                        .param("offset", "0")
                        .param("limit", "5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(productService).getAllProducts(0, 5);
    }

    @Test
    @DisplayName("should return bad request when limit is not a number")
    void shouldReturnBadRequestWhenLimitIsNotANumber() throws Exception {
        mockMvc.perform(get("/api/v1/products")
                        .param("offset", "0")
                        .param("limit", "xyz")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoMoreInteractions(productService);
    }

    @Test
    @DisplayName("should return bad request when offset is not a number")
    void shouldReturnBadRequestWhenOffsetIsNotANumber() throws Exception {
        mockMvc.perform(get("/api/v1/products")
                        .param("offset", "abc")
                        .param("limit", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoMoreInteractions(productService);
    }

}
