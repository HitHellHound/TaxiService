package com.efcon.ride.service;

import com.efcon.ride.broker.OutboundRideNotificationPublisher;
import com.efcon.ride.dto.RideInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class RideNotificationServiceImpl implements RideNotificationService {
    private final OutboundRideNotificationPublisher rideNotificationPublisher;
    private final DriverInfoService driverInfoService;

    @Override
    public void notifyDrivers(RideInfo rideInfo) {
        rideNotificationPublisher.notifyDrivers(rideInfo,
                new HashSet<>(driverInfoService.getSomeFreeDriverIds(10)));
    }
}
