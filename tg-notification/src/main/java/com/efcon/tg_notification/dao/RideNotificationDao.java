package com.efcon.tg_notification.dao;

import com.efcon.tg_notification.dto.RideInfo;

import java.util.Optional;

public interface RideNotificationDao {
    void pushRideNotificationQueue(Long driverId, Long rideId);
    Optional<RideInfo> popNextAndSetActiveRideNotification(Long driverId);
    void dropRideNotificationQueue(Long driverId);

    Optional<Long> getActiveRideNotificationId(Long driverId);
    boolean hasActiveRideNotification(Long driverId);
    void removeActiveRideNotification(Long driverId);

    void setRideAccepted(Long rideId);
    boolean isRideAccepted(Long rideId);

    void addRideInfo(Long rideId, RideInfo rideInfo);
    Optional<RideInfo> getRideInfo(Long rideId);
    void removeRideInfo(Long rideId);
}
