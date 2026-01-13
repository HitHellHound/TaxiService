package com.efcon.driver.broker;

import com.efcon.driver.event.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class DriverEventOutboundKafkaPublisher implements DriverEventOutboundPublisher {
    @Value("${message-broker.kafka.topic.driver-events}")
    private String driverEventTopicName;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final Logger LOGGER = LoggerFactory.getLogger(DriverEventOutboundKafkaPublisher.class);

    @Override
    public void publish(DriverCreatedEvent event) {
        logKafkaPublish(kafkaTemplate.send(driverEventTopicName, event.driverId().toString(), event), event);
    }

    @Override
    public void publish(DriverDeletedEvent event) {
        logKafkaPublish(kafkaTemplate.send(driverEventTopicName, event.driverId().toString(), event), event);
    }

    @Override
    public void publish(DriverChangedEvent event) {
        logKafkaPublish(kafkaTemplate.send(driverEventTopicName, event.driverId().toString(), event), event);
    }

    @Override
    public void publish(DriverShiftStartedEvent event) {
        logKafkaPublish(kafkaTemplate.send(driverEventTopicName, event.driverId().toString(), event), event);
    }

    @Override
    public void publish(DriverShiftEndedEvent event) {
        logKafkaPublish(kafkaTemplate.send(driverEventTopicName, event.driverId().toString(), event), event);
    }

    private void logKafkaPublish(CompletableFuture<SendResult<String, Object>> completableFuture, Object publishedEvent) {
        completableFuture.whenComplete((res, ex) -> {
            if (ex == null) {
                LOGGER.info("{} successfully published in topic {} partition {}: {}",
                        publishedEvent.getClass().getSimpleName(),
                        res.getRecordMetadata().topic(),
                        res.getRecordMetadata().partition(),
                        publishedEvent);
            } else {
                LOGGER.error("Publishing {} ({}) in topic {} partition {} ended with exception: {}",
                        publishedEvent.getClass().getSimpleName(),
                        publishedEvent,
                        res.getRecordMetadata().topic(),
                        res.getRecordMetadata().partition(),
                        ex.toString());
            }
        });
    }
}
