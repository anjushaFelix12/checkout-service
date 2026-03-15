package com.backend.checkout_service.offer.service;

import com.backend.checkout_service.offer.domain.Offer;
import com.backend.checkout_service.offer.dto.ActiveOfferResponse;
import com.backend.checkout_service.offer.dto.OfferRequest;
import com.backend.checkout_service.offer.exception.InvalidOfferException;
import com.backend.checkout_service.offer.repository.OfferRepository;
import com.backend.checkout_service.product.domain.Product;
import com.backend.checkout_service.product.exception.ProductNotFoundException;
import com.backend.checkout_service.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OfferServiceImplTest {

    @Mock
    private OfferRepository offerRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OfferServiceImpl offerService;

    private Product apple;
    private Offer existingOffer;
    private OfferRequest validRequest;

    @BeforeEach
    void setUp() {
        apple = new Product(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                "APPLE",
                "Apple",
                "piece",
                new BigDecimal("0.30")
        );

        existingOffer = new Offer(
                UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
                apple,
                2,
                new BigDecimal("0.45"),
                Instant.parse("2026-03-10T00:00:00Z"),
                Instant.parse("2026-03-17T23:59:59Z"),
                Instant.parse("2026-03-10T00:00:00Z")
        );

        validRequest = new OfferRequest(
                "APPLE",
                2,
                new BigDecimal("0.45")
        );
    }

    @Test
    @DisplayName("should return all active offers")
    void shouldReturnAllActiveOffers() {
        when(offerRepository.findAllActiveOffers(any(Instant.class)))
                .thenReturn(List.of(existingOffer));

        List<ActiveOfferResponse> result = offerService.findAll();

        assertEquals(1, result.size());
        ActiveOfferResponse response = result.get(0);

        assertEquals(existingOffer.getId(), response.id());
        assertEquals("APPLE", response.productCode());
        assertEquals(2, response.quantity());
        assertEquals(new BigDecimal("0.45"), response.bundlePrice());
        assertEquals("2 for €0,45", response.description());
        assertEquals(existingOffer.getValidUntil(), response.validUntil());

        verify(offerRepository).findAllActiveOffers(any(Instant.class));
        verifyNoMoreInteractions(offerRepository, productRepository);
    }

    @Test
    @DisplayName("should return empty list when there are no active offers")
    void shouldReturnEmptyListWhenThereAreNoActiveOffers() {
        when(offerRepository.findAllActiveOffers(any(Instant.class)))
                .thenReturn(List.of());

        List<ActiveOfferResponse> result = offerService.findAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(offerRepository).findAllActiveOffers(any(Instant.class));
        verifyNoMoreInteractions(offerRepository, productRepository);
    }

    @Test
    @DisplayName("should create offer successfully")
    void shouldCreateOfferSuccessfully() {
        when(productRepository.findByCode("APPLE")).thenReturn(Optional.of(apple));
        when(offerRepository.findByProductIdAndValidUntilAfter(eq(apple.getId()), any(Instant.class)))
                .thenReturn(Optional.empty());

        ArgumentCaptor<Offer> offerCaptor = ArgumentCaptor.forClass(Offer.class);

        when(offerRepository.save(any(Offer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ActiveOfferResponse response = offerService.createOffer(validRequest);

        verify(productRepository).findByCode("APPLE");
        verify(offerRepository).findByProductIdAndValidUntilAfter(eq(apple.getId()), any(Instant.class));
        verify(offerRepository).save(offerCaptor.capture());

        Offer savedOffer = offerCaptor.getValue();
        assertNotNull(savedOffer.getId());
        assertEquals(apple, savedOffer.getProduct());
        assertEquals(2, savedOffer.getQuantity());
        assertEquals(new BigDecimal("0.45"), savedOffer.getBundlePrice());
        assertNotNull(savedOffer.getCreatedAt());
        assertNotNull(savedOffer.getValidUntil());

        assertEquals("APPLE", response.productCode());
        assertEquals(2, response.quantity());
        assertEquals(new BigDecimal("0.45"), response.bundlePrice());
        assertEquals("2 for €0,45", response.description());
        assertNotNull(response.validUntil());
    }

    @Test
    @DisplayName("should throw when request is null")
    void shouldThrowWhenRequestIsNull() {
        InvalidOfferException ex = assertThrows(
                InvalidOfferException.class,
                () -> offerService.createOffer(null)
        );

        assertEquals("Offer request must not be null", ex.getMessage());
        verifyNoInteractions(offerRepository, productRepository);
    }

    @Test
    @DisplayName("should throw when product code is blank")
    void shouldThrowWhenProductCodeIsBlank() {
        OfferRequest request = new OfferRequest(" ", 2, new BigDecimal("0.45"));

        InvalidOfferException ex = assertThrows(
                InvalidOfferException.class,
                () -> offerService.createOffer(request)
        );

        assertEquals("Product code must not be blank", ex.getMessage());
        verifyNoInteractions(offerRepository, productRepository);
    }

    @Test
    @DisplayName("should throw when quantity is null")
    void shouldThrowWhenQuantityIsNull() {
        OfferRequest request = new OfferRequest("APPLE", null, new BigDecimal("0.45"));

        InvalidOfferException ex = assertThrows(
                InvalidOfferException.class,
                () -> offerService.createOffer(request)
        );

        assertEquals("Quantity must not be null", ex.getMessage());
        verifyNoInteractions(offerRepository, productRepository);
    }

    @Test
    @DisplayName("should throw when bundle price is null")
    void shouldThrowWhenBundlePriceIsNull() {
        OfferRequest request = new OfferRequest("APPLE", 2, null);

        InvalidOfferException ex = assertThrows(
                InvalidOfferException.class,
                () -> offerService.createOffer(request)
        );

        assertEquals("Bundle price must not be null", ex.getMessage());
        verifyNoInteractions(offerRepository, productRepository);
    }

    @Test
    @DisplayName("should throw when bundle price is zero")
    void shouldThrowWhenBundlePriceIsZero() {
        OfferRequest request = new OfferRequest("APPLE", 2, BigDecimal.ZERO);

        InvalidOfferException ex = assertThrows(
                InvalidOfferException.class,
                () -> offerService.createOffer(request)
        );

        assertEquals("Bundle price must be greater than 0", ex.getMessage());
        verifyNoInteractions(offerRepository, productRepository);
    }

    @Test
    @DisplayName("should throw when product does not exist")
    void shouldThrowWhenProductDoesNotExist() {
        when(productRepository.findByCode("APPLE")).thenReturn(Optional.empty());

        ProductNotFoundException ex = assertThrows(
                ProductNotFoundException.class,
                () -> offerService.createOffer(validRequest)
        );

        assertTrue(ex.getMessage().contains("APPLE"));
        verify(productRepository).findByCode("APPLE");
        verifyNoMoreInteractions(productRepository);
        verifyNoInteractions(offerRepository);
    }

    @Test
    @DisplayName("should throw when bundle price is not cheaper than regular total")
    void shouldThrowWhenBundlePriceIsNotCheaperThanRegularTotal() {
        OfferRequest request = new OfferRequest("APPLE", 2, new BigDecimal("0.60"));

        when(productRepository.findByCode("APPLE")).thenReturn(Optional.of(apple));

        InvalidOfferException ex = assertThrows(
                InvalidOfferException.class,
                () -> offerService.createOffer(request)
        );

        assertEquals("Bundle price must be less than regular total price (0.60)", ex.getMessage());
        verify(productRepository).findByCode("APPLE");
        verifyNoMoreInteractions(productRepository);
        verifyNoInteractions(offerRepository);
    }

    @Test
    @DisplayName("should throw when active weekly offer already exists")
    void shouldThrowWhenActiveWeeklyOfferAlreadyExists() {
        when(productRepository.findByCode("APPLE")).thenReturn(Optional.of(apple));
        when(offerRepository.findByProductIdAndValidUntilAfter(eq(apple.getId()), any(Instant.class)))
                .thenReturn(Optional.of(existingOffer));

        InvalidOfferException ex = assertThrows(
                InvalidOfferException.class,
                () -> offerService.createOffer(validRequest)
        );

        assertEquals(
                "An active weekly offer already exists for product code: APPLE",
                ex.getMessage()
        );

        verify(productRepository).findByCode("APPLE");
        verify(offerRepository).findByProductIdAndValidUntilAfter(eq(apple.getId()), any(Instant.class));
        verify(offerRepository, never()).save(any());
    }
}