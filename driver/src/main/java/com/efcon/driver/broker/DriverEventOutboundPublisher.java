package com.efcon.driver.broker;

import com.efcon.driver.event.*;

public interface DriverEventOutboundPublisher {
    void publish(DriverCreatedEvent event);
    void publish(DriverDeletedEvent event);
    void publish(DriverChangedEvent event);
    void publish(DriverShiftStartedEvent event);
    void publish(DriverShiftEndedEvent event);
}
