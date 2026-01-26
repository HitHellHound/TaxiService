package com.efcon.driver.event;

import com.efcon.driver.dto.DriverInfo;

public record DriverChangedEvent(Long driverId, DriverInfo driverInfo) {
}
