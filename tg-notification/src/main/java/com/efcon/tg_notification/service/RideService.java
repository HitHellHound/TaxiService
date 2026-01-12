package com.efcon.tg_notification.service;

public interface RideService {
    boolean accept(Long rideId, Long driverId);
}
