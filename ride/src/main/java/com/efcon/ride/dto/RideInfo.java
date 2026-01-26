package com.efcon.ride.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RideInfo(Long rideId, Long passengerId,
                       String startAddress, String destinationAddress,
                       LocalDateTime createdAt, BigDecimal price) {
}
