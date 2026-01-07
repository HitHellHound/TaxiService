package com.efcon.ride.exception;

public class ExternalBadRequestException extends RuntimeException {
    public ExternalBadRequestException(String message) {
        super(message);
    }
}
