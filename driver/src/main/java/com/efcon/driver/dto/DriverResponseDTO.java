package com.efcon.driver.dto;

public record DriverResponseDTO(Long id, CarResponseDTO car, String name, String email, String phone) {
}
