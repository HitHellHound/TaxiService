package com.efcon.driver.service;

import com.efcon.driver.dto.DriverRequest;
import com.efcon.driver.dto.DriverResponse;

public interface DriverService extends CRUDService<DriverRequest, DriverResponse> {
    DriverResponse attachCar(Long driverId, Long carId);
    void detachCar(Long driverId);
}
