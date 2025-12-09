package com.efcon.rating.dto;

public record RatingResponseDTO(Long id, Integer passengerScore, String passengerComment,
                                Integer driverScore, String driverComment) {
}
