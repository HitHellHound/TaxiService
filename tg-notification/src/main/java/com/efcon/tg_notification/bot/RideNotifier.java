package com.efcon.tg_notification.bot;

import com.efcon.tg_notification.dto.RideInfo;

public interface RideNotifier {
    void sendRideNotification(Long driverId, RideInfo rideInfo);
    void sendRideAcceptedNotification(Long driverId, RideInfo rideInfo);
    void sendRideRejectedNotification(Long driverId, Long rideId);
    void sendNotificationAcceptanceDecline(Long driverId, Long rideId, String reason);
    void sendRideNotificationExpired(Long driverId, Long rideId);
}
