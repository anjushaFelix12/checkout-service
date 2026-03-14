package com.backend.checkout_service.offer.repository;

import com.backend.checkout_service.offer.domain.Offer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OfferRepository extends JpaRepository<Offer, UUID> {

    @Query("""
            SELECT o
            FROM Offer o
            JOIN FETCH o.product
            WHERE o.product.id IN :productIds
              AND o.validUntil > :now
            """)
    List<Offer> findByProductIdInAndValidUntilAfter(
            @Param("productIds") Collection<UUID> productIds,
            @Param("now") Instant now
    );

    @Query("""
            SELECT o
            FROM Offer o
            JOIN FETCH o.product
            WHERE o.product.id = :productId
              AND o.validUntil > :now
            """)
    Optional<Offer> findByProductIdAndValidUntilAfter(
            @Param("productId") UUID productId,
            @Param("now") Instant now
    );

    @Query("""
            SELECT o
            FROM Offer o
            JOIN FETCH o.product
            WHERE o.validUntil > :now
            """)
    List<Offer> findAllActiveOffers(@Param("now") Instant now);
}