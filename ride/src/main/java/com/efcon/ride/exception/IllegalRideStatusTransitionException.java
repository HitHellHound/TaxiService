package com.efcon.ride.exception;

public class IllegalRideStatusTransitionException extends RuntimeException {
    public IllegalRideStatusTransitionException(String message) {
        super(message);
    }
}
