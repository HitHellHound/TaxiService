package com.efcon.ride.dto;

public record DriverResponse(Long id, CarResponse car, String name, String email, String phone) {
}
