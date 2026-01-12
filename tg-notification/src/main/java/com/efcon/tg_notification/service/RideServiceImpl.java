package com.efcon.tg_notification.service;

import com.efcon.tg_notification.dto.DriverAssignment;
import com.efcon.tg_notification.dto.RideResponse;
import com.efcon.tg_notification.exception.EntityNotFoundException;
import com.efcon.tg_notification.exception.ExternalBadRequestException;
import com.efcon.tg_notification.exception.ExternalServiceException;
import com.efcon.tg_notification.external.client.RideClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RideServiceImpl implements RideService {
    private final RideClient rideClient;
    @Override
    public Optional<RideResponse> accept(Long rideId, Long driverId) {
        try {
            return Optional.of(rideClient.acceptRide(rideId, new DriverAssignment(driverId)));
        } catch (EntityNotFoundException | ExternalServiceException | ExternalBadRequestException e) {
            return Optional.empty();
        }
    }
}
