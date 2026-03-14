package com.backend.checkout_service.offer.controller;

import com.backend.checkout_service.offer.dto.ActiveOfferResponse;
import com.backend.checkout_service.offer.dto.OfferRequest;
import com.backend.checkout_service.offer.service.OfferService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Offers", description = "Offer APIs")
@RestController
@RequestMapping("api/v1/offers")
public class OfferController {

    private final OfferService offerService;

    public OfferController(OfferService offerService) {
        this.offerService = offerService;
    }

    @GetMapping
    public ResponseEntity<List<ActiveOfferResponse>> getOffers() {
        return ResponseEntity.ok().body(offerService.findAll());
    }

    @PostMapping
    public ResponseEntity<ActiveOfferResponse> createOffer(@RequestBody OfferRequest request) {
        ActiveOfferResponse response = offerService.createOffer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
