package com.efcon.ride.event;

import com.efcon.ride.dto.RideInfo;

public record RideCreatedEvent(Long rideId, RideInfo rideInfo) {
}
