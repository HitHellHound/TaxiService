package com.efcon.ride.service;

import com.efcon.ride.model.DriverInfo;
import com.efcon.ride.model.DriverStatus;

public interface DriverInfoService {
    DriverInfo get(Long id);
    void save(DriverInfo driverInfo);
    void attachCar(Long id, Long carId);
    void detachCar(Long id);
    void changeDriverStatus(Long id, DriverStatus status);
    void delete(Long id);
}
