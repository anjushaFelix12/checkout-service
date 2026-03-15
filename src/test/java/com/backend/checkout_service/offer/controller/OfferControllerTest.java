package com.backend.checkout_service.offer.controller;

import com.backend.checkout_service.common.exception.GlobalExceptionHandler;
import com.backend.checkout_service.offer.dto.ActiveOfferResponse;
import com.backend.checkout_service.offer.dto.OfferRequest;
import com.backend.checkout_service.offer.exception.InvalidOfferException;
import com.backend.checkout_service.offer.service.OfferService;
import com.backend.checkout_service.product.exception.ProductNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OfferController.class)
@Import(GlobalExceptionHandler.class)
class OfferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OfferService offerService;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    class GetOffersTests {

        @Test
        @DisplayName("should return all active offers")
        void shouldReturnAllActiveOffers() throws Exception {
            List<ActiveOfferResponse> response = List.of(
                    new ActiveOfferResponse(
                            UUID.randomUUID(),
                            "APPLE",
                            2,
                            new BigDecimal("0.45"),
                            "2 for €0.45",
                            Instant.parse("2026-03-21T23:59:59Z")
                    ),
                    new ActiveOfferResponse(
                            UUID.randomUUID(),
                            "BANANA",
                            3,
                            new BigDecimal("1.20"),
                            "3 for €1.20",
                            Instant.parse("2026-03-21T23:59:59Z")
                    )
            );

            when(offerService.findAll()).thenReturn(response);

            mockMvc.perform(get("/api/v1/offers")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].productCode").value("APPLE"))
                    .andExpect(jsonPath("$[0].quantity").value(2))
                    .andExpect(jsonPath("$[0].bundlePrice").value(0.45))
                    .andExpect(jsonPath("$[0].description").value("2 for €0.45"))
                    .andExpect(jsonPath("$[1].productCode").value("BANANA"))
                    .andExpect(jsonPath("$[1].quantity").value(3))
                    .andExpect(jsonPath("$[1].bundlePrice").value(1.20))
                    .andExpect(jsonPath("$[1].description").value("3 for €1.20"));

            verify(offerService).findAll();
            verifyNoMoreInteractions(offerService);
        }

        @Test
        @DisplayName("should return empty list when no active offers exist")
        void shouldReturnEmptyListWhenNoActiveOffersExist() throws Exception {
            when(offerService.findAll()).thenReturn(List.of());

            mockMvc.perform(get("/api/v1/offers")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json("[]"));

            verify(offerService).findAll();
            verifyNoMoreInteractions(offerService);
        }

        @Test
        @DisplayName("should return internal server error when get offers fails unexpectedly")
        void shouldReturnInternalServerErrorWhenGetOffersFails() throws Exception {
            when(offerService.findAll()).thenThrow(new RuntimeException("Unexpected error"));

            mockMvc.perform(get("/api/v1/offers")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError());

            verify(offerService).findAll();
            verifyNoMoreInteractions(offerService);
        }
    }

    @Nested
    class CreateOfferTests {

        @Test
        @DisplayName("should create offer successfully")
        void shouldCreateOfferSuccessfully() throws Exception {
            OfferRequest request = new OfferRequest(
                    "APPLE",
                    2,
                    new BigDecimal("0.45")
            );

            ActiveOfferResponse response = new ActiveOfferResponse(
                    UUID.randomUUID(),
                    "APPLE",
                    2,
                    new BigDecimal("0.45"),
                    "2 for €0.45",
                    Instant.parse("2026-03-21T23:59:59Z")
            );

            when(offerService.createOffer(any(OfferRequest.class))).thenReturn(response);

            mockMvc.perform(post("/api/v1/offers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.productCode").value("APPLE"))
                    .andExpect(jsonPath("$.quantity").value(2))
                    .andExpect(jsonPath("$.bundlePrice").value(0.45))
                    .andExpect(jsonPath("$.description").value("2 for €0.45"));

            verify(offerService).createOffer(any(OfferRequest.class));
            verifyNoMoreInteractions(offerService);
        }

        @Test
        @DisplayName("should return bad request when product code is blank")
        void shouldReturnBadRequestWhenProductCodeIsBlank() throws Exception {
            OfferRequest request = new OfferRequest(
                    " ",
                    2,
                    new BigDecimal("0.45")
            );

            when(offerService.createOffer(any(OfferRequest.class)))
                    .thenThrow(new InvalidOfferException("Product code must not be blank"));

            mockMvc.perform(post("/api/v1/offers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(offerService).createOffer(any(OfferRequest.class));
            verifyNoMoreInteractions(offerService);
        }

        @Test
        @DisplayName("should return bad request when quantity is less than or equal to one")
        void shouldReturnBadRequestWhenQuantityIsInvalid() throws Exception {
            OfferRequest request = new OfferRequest(
                    "APPLE",
                    1,
                    new BigDecimal("0.45")
            );

            when(offerService.createOffer(any(OfferRequest.class)))
                    .thenThrow(new InvalidOfferException("Offer quantity must be greater than 1"));

            mockMvc.perform(post("/api/v1/offers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(offerService).createOffer(any(OfferRequest.class));
            verifyNoMoreInteractions(offerService);
        }

        @Test
        @DisplayName("should return bad request when bundle price is zero")
        void shouldReturnBadRequestWhenBundlePriceIsZero() throws Exception {
            OfferRequest request = new OfferRequest(
                    "APPLE",
                    2,
                    BigDecimal.ZERO
            );

            when(offerService.createOffer(any(OfferRequest.class)))
                    .thenThrow(new InvalidOfferException("Bundle price must be greater than 0"));

            mockMvc.perform(post("/api/v1/offers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(offerService).createOffer(any(OfferRequest.class));
            verifyNoMoreInteractions(offerService);
        }

        @Test
        @DisplayName("should return not found when product does not exist")
        void shouldReturnNotFoundWhenProductDoesNotExist() throws Exception {
            OfferRequest request = new OfferRequest(
                    "UNKNOWN_PRODUCT",
                    2,
                    new BigDecimal("0.45")
            );

            when(offerService.createOffer(any(OfferRequest.class)))
                    .thenThrow(new ProductNotFoundException("UNKNOWN_PRODUCT"));

            mockMvc.perform(post("/api/v1/offers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());

            verify(offerService).createOffer(any(OfferRequest.class));
            verifyNoMoreInteractions(offerService);
        }

        @Test
        @DisplayName("should return bad request when active offer already exists")
        void shouldReturnBadRequestWhenActiveOfferAlreadyExists() throws Exception {
            OfferRequest request = new OfferRequest(
                    "APPLE",
                    2,
                    new BigDecimal("0.45")
            );

            when(offerService.createOffer(any(OfferRequest.class)))
                    .thenThrow(new InvalidOfferException(
                            "An active weekly offer already exists for product code: APPLE"
                    ));

            mockMvc.perform(post("/api/v1/offers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(offerService).createOffer(any(OfferRequest.class));
            verifyNoMoreInteractions(offerService);
        }

        @Test
        @DisplayName("should return internal server error when create offer fails unexpectedly")
        void shouldReturnInternalServerErrorWhenCreateOfferFailsUnexpectedly() throws Exception {
            OfferRequest request = new OfferRequest(
                    "APPLE",
                    2,
                    new BigDecimal("0.45")
            );

            when(offerService.createOffer(any(OfferRequest.class)))
                    .thenThrow(new RuntimeException("Unexpected error"));

            mockMvc.perform(post("/api/v1/offers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError());

            verify(offerService).createOffer(any(OfferRequest.class));
            verifyNoMoreInteractions(offerService);
        }
    }
}