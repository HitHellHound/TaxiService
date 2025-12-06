package com.efcon.driver.service;

import com.efcon.driver.dto.DriverRequestDTO;
import com.efcon.driver.dto.DriverResponseDTO;

public interface DriverService extends CRUDService<DriverRequestDTO, DriverResponseDTO> {
    DriverResponseDTO attachCar(Long driverId, Long carId);
    void detachCar(Long driverId);
}
