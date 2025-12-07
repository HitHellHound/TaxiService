package com.efcon.ride.dto;

import java.math.BigDecimal;

public record RideRequestDTO(Long passengerId,
                             String startAddress, String destinationAddress,
                             BigDecimal price) {
}
