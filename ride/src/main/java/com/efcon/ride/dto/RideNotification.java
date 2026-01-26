package com.efcon.ride.dto;

import java.util.Set;

public record RideNotification(RideInfo rideInfo, Set<Long> driverIds) {
}
