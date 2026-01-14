package com.efcon.ride.broker;

import com.efcon.ride.dto.RideInfo;

import java.util.Set;

public interface OutboundRideNotificationPublisher {
    void notifyDrivers(RideInfo rideInfo, Set<Long> driverIds);
}
