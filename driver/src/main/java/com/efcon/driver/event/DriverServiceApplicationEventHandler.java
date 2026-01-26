package com.efcon.driver.event;

import com.efcon.driver.broker.DriverEventOutboundPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DriverServiceApplicationEventHandler {
    private final DriverEventOutboundPublisher driverEventOutboundPublisher;

    @EventListener
    public void onDriverCreatedEvent(DriverCreatedEvent event) {
        driverEventOutboundPublisher.publish(event);
    }

    @EventListener
    public void onDriverDeletedEvent(DriverDeletedEvent event) {
        driverEventOutboundPublisher.publish(event);
    }

    @EventListener
    public void onDriverChangedEvent(DriverChangedEvent event) {
        driverEventOutboundPublisher.publish(event);
    }

    @EventListener
    public void onDriverShiftStartedEvent(DriverShiftStartedEvent event) {
        driverEventOutboundPublisher.publish(event);
    }

    @EventListener
    public void onDriverShiftEndedEvent(DriverShiftEndedEvent event) {
        driverEventOutboundPublisher.publish(event);
    }
}
