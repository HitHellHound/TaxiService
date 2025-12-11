package com.efcon.rating.dto;

public record RatingResponse(Long id, Integer passengerScore, String passengerComment,
                             Integer driverScore, String driverComment) {
}
