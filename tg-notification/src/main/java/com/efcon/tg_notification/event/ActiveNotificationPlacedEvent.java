package com.efcon.tg_notification.event;

import com.efcon.tg_notification.dto.RideInfo;

public record ActiveNotificationPlacedEvent(Long driverId, RideInfo rideInfo) {
}
