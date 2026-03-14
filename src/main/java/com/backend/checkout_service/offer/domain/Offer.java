package com.backend.checkout_service.offer.domain;

import com.backend.checkout_service.product.domain.Product;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "offer")
public class Offer {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "bundle_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal bundlePrice;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "valid_until", nullable = false)
    private Instant validUntil;

    protected Offer() {
    }

    public Offer(UUID id,
                 Product product,
                 Integer quantity,
                 BigDecimal bundlePrice,
                 Instant createdAt,
                 Instant validUntil) {
        this.id = id;
        this.product = product;
        this.quantity = quantity;
        this.bundlePrice = bundlePrice;
        this.createdAt = createdAt;
        this.validUntil = validUntil;
    }

    public UUID getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getBundlePrice() {
        return bundlePrice;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getValidUntil() {
        return validUntil;
    }
}
