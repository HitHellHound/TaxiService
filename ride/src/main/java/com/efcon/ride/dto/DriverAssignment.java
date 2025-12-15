package com.efcon.ride.dto;

import jakarta.validation.constraints.NotNull;

public record DriverAssignment(@NotNull Long driverId) {
}
