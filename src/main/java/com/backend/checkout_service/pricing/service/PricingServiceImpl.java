package com.backend.checkout_service.pricing.service;

import com.backend.checkout_service.cart.domain.CartItem;
import com.backend.checkout_service.offer.domain.Offer;
import com.backend.checkout_service.offer.repository.OfferRepository;
import com.backend.checkout_service.pricing.dto.AppliedDiscount;
import com.backend.checkout_service.pricing.dto.PricingItemResult;
import com.backend.checkout_service.pricing.dto.PricingResult;
import com.backend.checkout_service.pricing.rule.PricingRule;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PricingServiceImpl implements PricingService {

    private final OfferRepository offerRepository;
    private final List<PricingRule> pricingRules;

    public PricingServiceImpl(OfferRepository offerRepository, List<PricingRule> pricingRules) {
        this.offerRepository = offerRepository;
        this.pricingRules = pricingRules;
    }

    @Override
    public PricingResult calculate(List<CartItem> cartItems) {
        List<PricingItemResult> itemResults = new ArrayList<>();

        List<UUID> productIds = cartItems.stream()
                .map(item -> item.getProduct().getId())
                .distinct()
                .toList();

        Map<UUID, Offer> offerMap = offerRepository
                .findByProductIdInAndValidUntilAfter(productIds, Instant.now())
                .stream()
                .collect(Collectors.toMap(
                        offer -> offer.getProduct().getId(),
                        Function.identity()
                ));

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;

        for (CartItem item : cartItems) {
            Offer offer = offerMap.get(item.getProduct().getId());

            PricingItemResult itemResult = pricingRules.stream()
                    .filter(rule -> rule.supports(offer))
                    .findFirst()
                    .map(rule -> rule.calculate(item, offer))
                    .orElseGet(() -> calculateWithoutOffer(item));

            itemResults.add(itemResult);

            subtotal = subtotal.add(itemResult.lineSubtotal());
            total = total.add(itemResult.lineTotal());

            BigDecimal itemDiscountTotal = itemResult.discounts()
                    .stream()
                    .map(AppliedDiscount::amount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            totalDiscount = totalDiscount.add(itemDiscountTotal);
        }

        return new PricingResult(
                itemResults,
                subtotal,
                totalDiscount,
                total
        );
    }

    private PricingItemResult calculateWithoutOffer(CartItem item) {
        BigDecimal subtotal = item.getProduct()
                .getUnitPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()));

        return new PricingItemResult(
                item.getProduct().getCode(),
                item.getProduct().getName(),
                item.getProduct().getUnit(),
                item.getProduct().getUnitPrice(),
                item.getQuantity(),
                subtotal,
                List.of(),
                subtotal
        );
    }
}