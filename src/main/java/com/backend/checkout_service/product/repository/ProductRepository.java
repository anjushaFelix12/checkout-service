package com.backend.checkout_service.product.repository;

import java.util.Optional;
import java.util.UUID;

import com.backend.checkout_service.product.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    Optional<Product> findByCode(String code);
}