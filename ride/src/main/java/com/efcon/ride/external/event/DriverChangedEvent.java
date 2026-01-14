package com.efcon.ride.external.event;

import com.efcon.ride.dto.OutboundDriverInfo;

public record DriverChangedEvent(Long driverId, OutboundDriverInfo driverInfo) {
}
