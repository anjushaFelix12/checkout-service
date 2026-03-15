package com.backend.checkout_service.product.service;

import com.backend.checkout_service.offer.domain.Offer;
import com.backend.checkout_service.offer.repository.OfferRepository;
import com.backend.checkout_service.product.domain.Product;
import com.backend.checkout_service.product.dto.ProductResponse;
import com.backend.checkout_service.product.exception.InvalidProductException;
import com.backend.checkout_service.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OfferRepository offerRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product apple;
    private Product banana;
    private Offer appleOffer;

    @BeforeEach
    void setUp() {
        apple = new Product(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                "APPLE",
                "Apple",
                "piece",
                new BigDecimal("0.30")
        );

        banana = new Product(
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                "BANANA",
                "Banana",
                "piece",
                new BigDecimal("0.50")
        );

        appleOffer = new Offer(
                UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
                apple,
                2,
                new BigDecimal("0.45"),
                Instant.parse("2026-03-10T00:00:00Z"),
                Instant.parse("2026-03-17T23:59:59Z"),
                Instant.parse("2026-03-10T00:00:00Z")
        );
    }

    @Test
    @DisplayName("should return paginated products with active offer when available")
    void shouldReturnPaginatedProductsWithActiveOfferWhenAvailable() {
        when(productRepository.findAll(PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(apple, banana)));

        when(offerRepository.findByProductIdInAndValidUntilAfter(any(), any(Instant.class)))
                .thenReturn(List.of(appleOffer));

        List<ProductResponse> result = productService.getAllProducts(0, 10);

        assertEquals(2, result.size());

        ProductResponse first = result.get(0);
        assertEquals(apple.getId(), first.id());
        assertEquals("APPLE", first.code());
        assertEquals("Apple", first.name());
        assertEquals("piece", first.unit());
        assertEquals(new BigDecimal("0.30"), first.unitPrice());
        assertNotNull(first.activeOffer());
        assertEquals(2, first.activeOffer().quantity());
        assertEquals(new BigDecimal("0.45"), first.activeOffer().bundlePrice());
        assertEquals("2 for €0.45", first.activeOffer().description());

        ProductResponse second = result.get(1);
        assertEquals(banana.getId(), second.id());
        assertEquals("BANANA", second.code());
        assertNull(second.activeOffer());

        verify(productRepository).findAll(PageRequest.of(0, 10));
        verify(offerRepository).findByProductIdInAndValidUntilAfter(any(), any(Instant.class));
        verifyNoMoreInteractions(productRepository, offerRepository);
    }

    @Test
    @DisplayName("should return empty list when no products exist")
    void shouldReturnEmptyListWhenNoProductsExist() {
        when(productRepository.findAll(PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of()));

        when(offerRepository.findByProductIdInAndValidUntilAfter(any(), any(Instant.class)))
                .thenReturn(List.of());

        List<ProductResponse> result = productService.getAllProducts(0, 10);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(productRepository).findAll(PageRequest.of(0, 10));
        verify(offerRepository).findByProductIdInAndValidUntilAfter(any(), any(Instant.class));
        verifyNoMoreInteractions(productRepository, offerRepository);
    }

    @Test
    @DisplayName("should throw when limit is zero")
    void shouldThrowWhenLimitIsZero() {
        InvalidProductException ex = assertThrows(
                InvalidProductException.class,
                () -> productService.getAllProducts(0, 0)
        );

        assertEquals("Limit must be greater than zero", ex.getMessage());
        verifyNoInteractions(productRepository, offerRepository);
    }

    @Test
    @DisplayName("should throw when limit is negative")
    void shouldThrowWhenLimitIsNegative() {
        InvalidProductException ex = assertThrows(
                InvalidProductException.class,
                () -> productService.getAllProducts(0, -1)
        );

        assertEquals("Limit must be greater than zero", ex.getMessage());
        verifyNoInteractions(productRepository, offerRepository);
    }

    @Test
    @DisplayName("should throw when offset is negative")
    void shouldThrowWhenOffsetIsNegative() {
        InvalidProductException ex = assertThrows(
                InvalidProductException.class,
                () -> productService.getAllProducts(-10, 10)
        );

        assertEquals("Offset must not be negative", ex.getMessage());
        verifyNoInteractions(productRepository, offerRepository);
    }
}