package com.efcon.tg_notification.service;

import com.efcon.tg_notification.dto.RideResponse;

import java.util.Optional;

public interface RideService {
    Optional<RideResponse> accept(Long rideId, Long driverId);
}
