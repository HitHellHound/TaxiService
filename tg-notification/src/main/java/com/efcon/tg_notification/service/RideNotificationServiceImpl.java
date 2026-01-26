package com.efcon.tg_notification.service;

import com.efcon.tg_notification.dao.RideNotificationDao;
import com.efcon.tg_notification.dto.RideInfo;
import com.efcon.tg_notification.dto.RideNotification;
import com.efcon.tg_notification.event.ActiveNotificationPlacedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RideNotificationServiceImpl implements RideNotificationService {
    private final ApplicationEventPublisher publisher;
    private final RideNotificationDao notificationDao;

    @Override
    public void notifyDrivers(RideNotification notification) {
        notificationDao.addRideInfo(notification.rideInfo().rideId(), notification.rideInfo());
        notificationDao.pushRideNotificationQueues(notification.driverIds(), notification.rideInfo().rideId());

        for (Long driverId: notification.driverIds()) {
            Optional<RideInfo> newActiveNotification = notificationDao.popNextAndSetActiveRideNotification(driverId, true);
            newActiveNotification.ifPresent(rideInfo ->
                    publisher.publishEvent(new ActiveNotificationPlacedEvent(driverId, rideInfo)));
        }
    }
}
