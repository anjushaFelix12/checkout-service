package com.backend.checkout_service.offer.service;

import com.backend.checkout_service.offer.domain.Offer;
import com.backend.checkout_service.offer.dto.ActiveOfferResponse;
import com.backend.checkout_service.offer.dto.OfferRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

public interface OfferService {

    List<ActiveOfferResponse> findAll();

    ActiveOfferResponse createOffer(@Valid @RequestBody OfferRequest offerRequest);
}
