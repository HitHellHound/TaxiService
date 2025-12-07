package com.efcon.ride.dto;

import jakarta.validation.constraints.NotNull;

public record DriverAssignmentDto(@NotNull Long driverId) {
}
