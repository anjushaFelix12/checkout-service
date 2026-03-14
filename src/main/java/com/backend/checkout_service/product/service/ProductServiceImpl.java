package com.backend.checkout_service.product.service;

import com.backend.checkout_service.offer.domain.Offer;
import com.backend.checkout_service.offer.repository.OfferRepository;
import com.backend.checkout_service.product.domain.Product;
import com.backend.checkout_service.product.dto.ActiveOffer;
import com.backend.checkout_service.product.dto.ProductResponse;
import com.backend.checkout_service.product.repository.ProductRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final OfferRepository offerRepository;

    public ProductServiceImpl(ProductRepository productRepository,
                              OfferRepository offerRepository) {
        this.productRepository = productRepository;
        this.offerRepository = offerRepository;
    }

    @Override
    public List<ProductResponse> getAllProducts(int offset, int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be greater than zero");
        }
        if (offset < 0) {
            throw new IllegalArgumentException("Offset must not be negative");
        }
        if (offset % limit != 0) {
            throw new IllegalArgumentException("Offset must be a multiple of limit");
        }

        Pageable pageable = PageRequest.of(offset / limit, limit);
        List<Product> products = productRepository.findAll(pageable).getContent();

        List<UUID> productIds = products.stream()
                .map(Product::getId)
                .toList();

        Map<UUID, Offer> offerMap = offerRepository
                .findByProductIdInAndValidUntilAfter(productIds, Instant.now())
                .stream()
                .collect(Collectors.toMap(
                        offer -> offer.getProduct().getId(),
                        Function.identity()
                ));

        return products.stream()
                .map(product -> {
                    Offer offer = offerMap.get(product.getId());

                    ActiveOffer activeOffer = null;
                    if (offer != null) {
                        activeOffer = new ActiveOffer(
                                offer.getQuantity(),
                                offer.getBundlePrice(),
                                offer.getQuantity() + " for €" + offer.getBundlePrice()
                        );
                    }

                    return new ProductResponse(
                            product.getId(),
                            product.getCode(),
                            product.getName(),
                            product.getUnit(),
                            product.getUnitPrice(),
                            activeOffer
                    );
                })
                .toList();
    }
}

