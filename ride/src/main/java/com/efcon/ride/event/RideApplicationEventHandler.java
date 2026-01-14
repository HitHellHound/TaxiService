package com.efcon.ride.event;

import com.efcon.ride.service.RideNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RideApplicationEventHandler {
    private final RideNotificationService notificationService;

    @EventListener
    public void onRideCreatedEvent(RideCreatedEvent event) {
        notificationService.notifyDrivers(event.rideInfo());
    }

    @EventListener
    public void onRideAcceptedEvent(RideAcceptedEvent event) {

    }

    @EventListener
    public void onRideCompletedEvent(RideCompletedEvent event) {

    }

    @EventListener
    public void onRideCanceledEvent(RideCanceledEvent event) {

    }
}
