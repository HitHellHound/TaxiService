package com.efcon.ride.scheduler;

import com.efcon.ride.service.RideNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StaleRideNotificationsResendScheduler {
    private final RideNotificationService notificationService;

    @Scheduled(initialDelayString = "${mq.ride-notifications.resend.rate.ms}",
            fixedRateString = "${mq.ride-notifications.resend.rate.ms}")
    public void resend() {
        notificationService.resendStaleNotifications();
    }
}
