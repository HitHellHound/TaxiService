package com.efcon.rating.exception;

public class ExternalBadRequestException extends RuntimeException {
    public ExternalBadRequestException(String message) {
        super(message);
    }
}
