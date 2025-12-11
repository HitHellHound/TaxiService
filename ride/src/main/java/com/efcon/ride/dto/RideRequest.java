package com.efcon.ride.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record RideRequest(@NotNull Long passengerId,
                          @NotBlank @Size(max = 255) String startAddress,
                          @NotBlank @Size(max = 255) String destinationAddress,
                          @NotNull @Positive @Digits(integer = 10, fraction = 2) BigDecimal price) {
}
