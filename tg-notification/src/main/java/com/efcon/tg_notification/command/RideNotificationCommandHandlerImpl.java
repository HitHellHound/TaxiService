package com.efcon.tg_notification.command;

import com.efcon.tg_notification.dao.RideNotificationDao;
import com.efcon.tg_notification.dto.RideInfo;
import com.efcon.tg_notification.event.AcceptanceDeclinedEvent;
import com.efcon.tg_notification.event.ActiveNotificationPlacedEvent;
import com.efcon.tg_notification.event.RideAcceptedEvent;
import com.efcon.tg_notification.event.RideRejectedEvent;
import com.efcon.tg_notification.service.RideService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RideNotificationCommandHandlerImpl implements RideNotificationCommandHandler {
    private final RideService rideService;
    private final ApplicationEventPublisher publisher;
    private final RideNotificationDao notificationDao;

    @Override
    public void handle(AcceptRideNotificationCommand command) {
        Optional<RideInfo> rideInfo = notificationDao.getRideInfo(command.rideId());

        if (rideInfo.isPresent() && !notificationDao.isRideAccepted(command.rideId())) {
            boolean isAccepted = rideService.accept(command.rideId(), command.driverId());

            if (isAccepted) {
                notificationDao.dropRideNotificationQueue(command.driverId());
                notificationDao.removeActiveRideNotification(command.driverId());
                notificationDao.setRideAccepted(command.rideId());
                publisher.publishEvent(new RideAcceptedEvent(command.driverId(), rideInfo.get()));
                return;
            }
        }

        publisher.publishEvent(new AcceptanceDeclinedEvent(command.driverId(), command.rideId(),
                "Ride #" +  command.rideId() + " has already changed status"));

        Optional<RideInfo> newActiveRide = notificationDao.popNextAndSetActiveRideNotification(command.driverId());
        newActiveRide.ifPresent(newRideInfo ->
                publisher.publishEvent(new ActiveNotificationPlacedEvent(command.driverId(), newRideInfo)));
    }

    @Override
    public void handle(RejectRideNotificationCommand command) {
        publisher.publishEvent(new RideRejectedEvent(command.driverId(), command.rideId()));

        Optional<RideInfo> newActiveRide = notificationDao.popNextAndSetActiveRideNotification(command.driverId());
        newActiveRide.ifPresent(rideInfo ->
                publisher.publishEvent(new ActiveNotificationPlacedEvent(command.driverId(), rideInfo)));
    }
}
