package com.efcon.tg_notification.event;

public record AcceptanceDeclinedEvent(Long driverId, Long rideId, String reason) {
}
