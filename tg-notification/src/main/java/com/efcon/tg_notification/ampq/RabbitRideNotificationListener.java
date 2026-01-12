package com.efcon.tg_notification.ampq;

import com.efcon.tg_notification.dto.RideNotification;
import com.efcon.tg_notification.service.RideNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RabbitRideNotificationListener {
    private final RideNotificationService notificationService;

    @RabbitListener(queues = "${mq.ride-notifications.send}")
    public void receiveRideNotification(RideNotification notification) {
        notificationService.notifyDrivers(notification);
    }
}
