package com.efcon.tg_notification.event;

import com.efcon.tg_notification.aop.IgnoreExceptions;
import com.efcon.tg_notification.bot.RideNotifier;
import com.efcon.tg_notification.exception.DriverChatNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@IgnoreExceptions(exceptions = {DriverChatNotFoundException.class})
public class NotificationEventHandler {
    private final RideNotifier notifier;

    @EventListener
    public void handleActiveNotificationPlaced(ActiveNotificationPlacedEvent event) {
        notifier.sendRideNotification(event.driverId(), event.rideInfo());
    }

    @EventListener
    public void handleNotificationAcceptedEvent(RideAcceptedEvent event) {
        notifier.sendRideAcceptedNotification(event.driverId(), event.rideInfo());
    }

    @EventListener
    public void handleNotificationRejectedEvent(RideRejectedEvent event) {
        notifier.sendRideRejectedNotification(event.driverId(), event.rideId());
    }

    @EventListener
    public void handleNotificationRejectedEvent(AcceptanceDeclinedEvent event) {
        notifier.sendNotificationAcceptanceDecline(event.driverId(), event.rideId(), event.reason());
    }
}
