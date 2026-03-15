package com.backend.checkout_service.cart.repository;

import com.backend.checkout_service.cart.domain.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

    List<CartItem> findByCartId(UUID cartId);

    Optional<CartItem> findByCartIdAndProductId(UUID cartId, UUID productId);

    @Query("""
                select ci
                from CartItem ci
                join fetch ci.product
                where ci.cart.id = :cartId
            """)
    List<CartItem> findAllByCartIdWithProduct(UUID cartId);

    void deleteByCartIdAndProductId(UUID cartId, UUID productId);

}