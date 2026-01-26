package com.efcon.driver.event;

import com.efcon.driver.dto.DriverInfo;

public record DriverCreatedEvent(Long driverId, DriverInfo driverInfo) {
}
