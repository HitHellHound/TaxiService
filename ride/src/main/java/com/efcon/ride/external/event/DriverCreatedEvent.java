package com.efcon.ride.external.event;

import com.efcon.ride.dto.OutboundDriverInfo;

public record DriverCreatedEvent(Long driverId, OutboundDriverInfo driverInfo) {
}
