package com.backend.checkout_service.product.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(
        name = "product",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_product_code", columnNames = "code")
        }
)
public class Product {

    @Id
    private UUID id;

    @Column(name = "code", nullable = false, length = 50, unique = true)
    private String code;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "unit", nullable = false, length = 50)
    private String unit;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    public Product() {
    }

    public Product(UUID id, String code, String name, String unit, BigDecimal unitPrice) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.unit = unit;
        this.unitPrice = unitPrice;
    }

}