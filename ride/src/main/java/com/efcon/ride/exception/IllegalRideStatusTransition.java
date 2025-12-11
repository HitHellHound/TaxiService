package com.efcon.ride.exception;

public class IllegalRideStatusTransition extends RuntimeException {
    public IllegalRideStatusTransition(String message) {
        super(message);
    }
}
