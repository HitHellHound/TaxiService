package com.efcon.ride.dto;

import com.efcon.ride.model.RideStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RideResponse(Long id, Long driverId, Long passengerId,
                           String startAddress, String destinationAddress, RideStatus status,
                           LocalDateTime createdAt, BigDecimal price) {
}
