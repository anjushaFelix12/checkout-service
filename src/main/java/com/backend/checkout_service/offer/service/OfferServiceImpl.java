package com.backend.checkout_service.offer.service;

import com.backend.checkout_service.offer.domain.Offer;
import com.backend.checkout_service.offer.dto.ActiveOfferResponse;
import com.backend.checkout_service.offer.dto.OfferRequest;
import com.backend.checkout_service.offer.exception.InvalidOfferException;
import com.backend.checkout_service.offer.repository.OfferRepository;
import com.backend.checkout_service.product.domain.Product;
import com.backend.checkout_service.product.exception.ProductNotFoundException;
import com.backend.checkout_service.product.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OfferServiceImpl implements OfferService {

    private final OfferRepository offerRepository;
    private final ProductRepository productRepository;

    public OfferServiceImpl(OfferRepository offerRepository, ProductRepository productRepository) {
        this.offerRepository = offerRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActiveOfferResponse> findAll() {
        return offerRepository.findAllActiveOffers(Instant.now())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public ActiveOfferResponse createOffer(OfferRequest request) {
        validateRequest(request);

        Product product = productRepository.findByCode(request.productCode())
                .orElseThrow(() -> new ProductNotFoundException(request.productCode()));

        validatePricingRules(product, request);

        Instant now = Instant.now();

        Optional<Offer> existingOffer = offerRepository
                .findByProductIdAndValidUntilAfter(product.getId(), now);

        if (existingOffer.isPresent()) {
            throw new InvalidOfferException(
                    "An active weekly offer already exists for product code: " + request.productCode()
            );
        }

        Offer offer = new Offer(
                UUID.randomUUID(),
                product,
                request.quantity(),
                request.bundlePrice(),
                now,
                now.plus(7, ChronoUnit.DAYS),
                now
        );

        Offer savedOffer = offerRepository.save(offer);

        return mapToResponse(savedOffer, product.getCode());
    }

    private void validateRequest(OfferRequest request) {
        if (request == null) {
            throw new InvalidOfferException("Offer request must not be null");
        }

        if (request.productCode() == null || request.productCode().isBlank()) {
            throw new InvalidOfferException("Product code must not be blank");
        }

        if (request.quantity() == null) {
            throw new InvalidOfferException("Quantity must not be null");
        }

        if (request.bundlePrice() == null) {
            throw new InvalidOfferException("Bundle price must not be null");
        }

        if (request.bundlePrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOfferException("Bundle price must be greater than 0");
        }
    }

    private void validatePricingRules(Product product, OfferRequest request) {
        BigDecimal regularTotal = product.getUnitPrice()
                .multiply(BigDecimal.valueOf(request.quantity()));

        if (request.bundlePrice().compareTo(regularTotal) >= 0) {
            throw new InvalidOfferException(
                    "Bundle price must be less than regular total price (" + regularTotal + ")"
            );
        }
    }

    private ActiveOfferResponse mapToResponse(Offer offer) {
        return new ActiveOfferResponse(
                offer.getId(),
                offer.getProduct().getCode(),
                offer.getQuantity(),
                offer.getBundlePrice(),
                String.format("%d for €%.2f", offer.getQuantity(), offer.getBundlePrice()),
                offer.getValidUntil()
        );
    }

    private ActiveOfferResponse mapToResponse(Offer offer, String productCode) {
        return new ActiveOfferResponse(
                offer.getId(),
                productCode,
                offer.getQuantity(),
                offer.getBundlePrice(),
                String.format("%d for €%.2f", offer.getQuantity(), offer.getBundlePrice()),
                offer.getValidUntil()
        );
    }
}
