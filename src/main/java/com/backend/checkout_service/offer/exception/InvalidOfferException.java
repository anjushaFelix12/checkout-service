package com.backend.checkout_service.offer.exception;

public class InvalidOfferException extends RuntimeException {

    public InvalidOfferException(String message) {
        super(message);
    }
}
