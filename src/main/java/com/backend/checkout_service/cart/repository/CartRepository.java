package com.backend.checkout_service.cart.repository;

import com.backend.checkout_service.cart.domain.Cart;
import com.backend.checkout_service.cart.domain.CartStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CartRepository extends JpaRepository<Cart, UUID> {

    @Query("""
            SELECT DISTINCT c
            FROM Cart c
            LEFT JOIN FETCH c.items i
            LEFT JOIN FETCH i.product
            WHERE c.id = :cartId
            """)
    Optional<Cart> findByIdWithItems(@Param("cartId") UUID cartId);

}