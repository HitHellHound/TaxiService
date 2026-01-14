package com.efcon.ride.broker;

import com.efcon.ride.dto.RideInfo;
import com.efcon.ride.dto.RideNotification;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class OutboundRideNotificationRabbitMqPublisher implements OutboundRideNotificationPublisher {
    private final RabbitTemplate rabbitTemplate;
    @Value("${mq.ride-notifications.send.exchange}")
    private String exchangeName;
    @Value("${mq.ride-notifications.send.queue}")
    private String queueName;

    @Override
    public void notifyDrivers(RideInfo rideInfo, Set<Long> driverIds) {
        rabbitTemplate.convertAndSend(exchangeName, queueName, new RideNotification(rideInfo, driverIds));
    }
}
