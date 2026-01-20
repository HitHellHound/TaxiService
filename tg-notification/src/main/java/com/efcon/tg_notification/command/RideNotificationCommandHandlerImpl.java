package com.efcon.tg_notification.command;

import com.efcon.tg_notification.dao.RideNotificationDao;
import com.efcon.tg_notification.dto.ExpiredNotificationTuple;
import com.efcon.tg_notification.dto.RideInfo;
import com.efcon.tg_notification.dto.RideResponse;
import com.efcon.tg_notification.event.*;
import com.efcon.tg_notification.service.RideService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RideNotificationCommandHandlerImpl implements RideNotificationCommandHandler {
    private final RideService rideService;
    private final ApplicationEventPublisher publisher;
    private final RideNotificationDao notificationDao;

    @Override
    public void handle(AcceptRideNotificationCommand command) {
        Optional<RideInfo> rideInfo = notificationDao.tryGetRideInfoForAcceptance(command.rideId(), command.driverId());

        if (rideInfo.isPresent()) {
            Optional<RideResponse> rideResponse = rideService.accept(command.rideId(), command.driverId());

            if (rideResponse.isPresent()) {
                notificationDao.setRideAcceptedAndFlushQueue(command.rideId(), command.driverId());
                publisher.publishEvent(new RideAcceptedEvent(command.driverId(), rideInfo.get()));
                return;
            }
        }

        publisher.publishEvent(new AcceptanceDeclinedEvent(command.driverId(), command.rideId(),
                "Ride #" +  command.rideId() + " has already changed status"));

        Optional<RideInfo> newActiveNotification = notificationDao
                .popNextAndSetActiveRideNotification(command.driverId(), false);
        newActiveNotification.ifPresent(newRideInfo ->
                publisher.publishEvent(new ActiveNotificationPlacedEvent(command.driverId(), newRideInfo)));
    }

    @Override
    public void handle(RejectRideNotificationCommand command) {
        publisher.publishEvent(new RideRejectedEvent(command.driverId(), command.rideId()));

        Optional<RideInfo> newActiveNotification = notificationDao
                .popNextAndSetActiveRideNotification(command.driverId(), false);
        newActiveNotification.ifPresent(rideInfo ->
                publisher.publishEvent(new ActiveNotificationPlacedEvent(command.driverId(), rideInfo)));
    }

    @Override
    public void handle(ExpireRideNotificationsCommand command) {
        List<ExpiredNotificationTuple> expiredNotifications = notificationDao.getExpiredNotifications();

        for (ExpiredNotificationTuple expiredNotification : expiredNotifications) {
            boolean isExpired = notificationDao.tryToExpireActiveNotification(expiredNotification.driverId(),
                    expiredNotification.rideId());

            if (!isExpired) {
                continue;
            }

            publisher.publishEvent(new RideNotificationExpiredEvent(expiredNotification.driverId(),
                    expiredNotification.rideId()));

            Optional<RideInfo> newActiveNotification = notificationDao
                    .popNextAndSetActiveRideNotification(expiredNotification.driverId(), true);
            newActiveNotification.ifPresent(rideInfo ->
                    publisher.publishEvent(new ActiveNotificationPlacedEvent(expiredNotification.driverId(), rideInfo)));
        }
    }
}
