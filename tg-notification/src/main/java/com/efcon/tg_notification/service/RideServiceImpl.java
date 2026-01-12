package com.efcon.tg_notification.service;

import org.springframework.stereotype.Component;

@Component
public class RideServiceImpl implements RideService {
    @Override
    public boolean accept(Long rideId, Long driverId) {
        return true;
    }
}
