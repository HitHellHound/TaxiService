package com.efcon.ride.service;

import com.efcon.ride.dto.RideRequestDTO;
import com.efcon.ride.dto.RideResponseDTO;

public interface RideService extends CRUDService<RideRequestDTO, RideResponseDTO> {
    RideResponseDTO accept(Long id, Long driverId);
    RideResponseDTO driveToPassenger(Long id);
    RideResponseDTO driveToDestination(Long id);
    RideResponseDTO complete(Long id);
    RideResponseDTO cancel(Long id);
}
