package com.efcon.ride.service;

import com.efcon.ride.broker.OutboundRideNotificationPublisher;
import com.efcon.ride.dao.RideNotificationDao;
import com.efcon.ride.dto.RideInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RideNotificationServiceImpl implements RideNotificationService {
    private final OutboundRideNotificationPublisher rideNotificationPublisher;
    private final DriverInfoService driverInfoService;
    private final RideNotificationDao rideNotificationDao;

    @Value("${mq.ride-notifications.resend.stale-after.seconds}")
    private Integer notificationStaleAfter;

    @Override
    public void notifyDrivers(RideInfo rideInfo) {
        rideNotificationDao.create(rideInfo);
        rideNotificationPublisher.notifyDrivers(rideInfo,
                new HashSet<>(driverInfoService.getSomeFreeDriverIds(10)));
    }

    @Override
    public void closeNotification(Long rideId) {
        rideNotificationDao.closeNotificationById(rideId);
    }

    @Override
    public void resendStaleNotifications() {
        List<RideInfo> rideInfoToResend = rideNotificationDao.findStaleOpenedNotifications(notificationStaleAfter);
        rideNotificationDao.updateLastNotifiedByIds(rideInfoToResend
                .stream()
                .map(RideInfo::rideId)
                .toList());
        rideInfoToResend.forEach(rideInfo -> rideNotificationPublisher.notifyDrivers(rideInfo,
                new HashSet<>(driverInfoService.getSomeFreeDriverIds(10))));
    }
}
