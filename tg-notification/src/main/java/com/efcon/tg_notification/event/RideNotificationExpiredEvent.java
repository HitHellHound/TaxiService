package com.efcon.tg_notification.event;

public record RideNotificationExpiredEvent(Long driverId, Long rideId) {
}
