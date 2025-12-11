package com.efcon.ride.service;

import com.efcon.ride.dto.RideRequest;
import com.efcon.ride.dto.RideResponse;

public interface RideService extends CRUDService<RideRequest, RideResponse> {
    RideResponse accept(Long id, Long driverId);
    RideResponse driveToPassenger(Long id);
    RideResponse driveToDestination(Long id);
    RideResponse complete(Long id);
    RideResponse cancel(Long id);
}
