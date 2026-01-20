package com.efcon.tg_notification.scheduler;

import com.efcon.tg_notification.command.ExpireRideNotificationsCommand;
import com.efcon.tg_notification.command.RideNotificationCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExpiredRideNotificationsScheduler {
    private final RideNotificationCommandHandler rideNotificationCommandHandler;

    @Scheduled(initialDelayString = "${ride-notifications.driver.active-notification.expire-cleaning-rate-in-ms}",
            fixedDelayString = "${ride-notifications.driver.active-notification.expire-cleaning-rate-in-ms}")
    public void expire() {
        rideNotificationCommandHandler.handle(new ExpireRideNotificationsCommand());
    }
}
