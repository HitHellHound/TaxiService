package com.efcon.tg_notification.bot;

import com.efcon.tg_notification.dto.RideInfo;

public interface RideNotifier {
    void sendRideNotification(Long driverId, RideInfo rideInfo);
}
