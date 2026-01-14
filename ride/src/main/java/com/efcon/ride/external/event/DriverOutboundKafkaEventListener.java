package com.efcon.ride.external.event;

import com.efcon.ride.model.DriverInfo;
import com.efcon.ride.model.DriverStatus;
import com.efcon.ride.service.DriverInfoService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@KafkaListener(topics = "${outbound.events.kafka.topic.driver-events}")
public class DriverOutboundKafkaEventListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(DriverOutboundKafkaEventListener.class);

    private final DriverInfoService driverInfoService;

    @KafkaHandler
    public void onDriverCreatedEvent(DriverCreatedEvent event, Acknowledgment ack) {
        try {
            driverInfoService.save(new DriverInfo(event.driverId(), null, DriverStatus.FREE));
            ack.acknowledge();
            logSuccessfulEventProcessing(event);
        } catch (Exception exception) {
            logUnsuccessfulEventProcessing(event, exception);
        }
    }

    @KafkaHandler
    public void onDriverDeletedEvent(DriverDeletedEvent event, Acknowledgment ack) {
        try {
            driverInfoService.delete(event.driverId());
            ack.acknowledge();
            logSuccessfulEventProcessing(event);
        } catch (Exception exception) {
            logUnsuccessfulEventProcessing(event, exception);
        }
    }

    @KafkaHandler
    public void onDriverChangedEvent(DriverChangedEvent event, Acknowledgment ack) {
        try {
            ack.acknowledge();
            logSuccessfulEventProcessing(event);
        } catch (Exception exception) {
            logUnsuccessfulEventProcessing(event, exception);
        }
    }

    @KafkaHandler
    public void onDriverShiftStartedEvent(DriverShiftStartedEvent event, Acknowledgment ack) {
        try {
            driverInfoService.attachCar(event.driverId(), event.carId());
            ack.acknowledge();
            logSuccessfulEventProcessing(event);
        } catch (Exception exception) {
            logUnsuccessfulEventProcessing(event, exception);
        }
    }

    @KafkaHandler
    public void onDriverShiftEndedEvent(DriverShiftEndedEvent event, Acknowledgment ack) {
        try {
            driverInfoService.detachCar(event.driverId());
            ack.acknowledge();
            logSuccessfulEventProcessing(event);
        } catch (Exception exception) {
            logUnsuccessfulEventProcessing(event, exception);
        }
    }

    private void logSuccessfulEventProcessing(Object event) {
        LOGGER.info("Successfully process {}: {}",
                event.getClass().getSimpleName(),
                event);
    }

    private void logUnsuccessfulEventProcessing(Object event, Exception exception) {
        LOGGER.error("Exception {} threw while processing {} ({}): {}. Offset doesn't change",
                exception.getClass().getName(),
                event.getClass().getSimpleName(),
                event,
                exception.getMessage());
    }
}
