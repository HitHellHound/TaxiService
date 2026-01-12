package com.efcon.tg_notification.service;

import com.efcon.tg_notification.dto.RideNotification;

public interface RideNotificationService {
    void notifyDrivers(RideNotification notification);
}
