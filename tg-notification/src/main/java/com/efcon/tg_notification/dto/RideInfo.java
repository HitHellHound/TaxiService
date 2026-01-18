package com.efcon.tg_notification.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RideInfo(Long rideId,
                       String startAddress, String destinationAddress,
                       LocalDateTime createdAt, BigDecimal price) implements Serializable {
}
