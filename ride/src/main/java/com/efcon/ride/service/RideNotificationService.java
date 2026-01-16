package com.efcon.ride.service;

import com.efcon.ride.dto.RideInfo;

public interface RideNotificationService {
    void notifyDrivers(RideInfo rideInfo);
    void closeNotification(Long rideId);
    void resendStaleNotifications();
}
