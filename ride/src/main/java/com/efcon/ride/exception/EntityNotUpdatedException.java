package com.efcon.ride.exception;

public class EntityNotUpdatedException extends RuntimeException {
    public EntityNotUpdatedException(String message) {
        super(message);
    }
}
