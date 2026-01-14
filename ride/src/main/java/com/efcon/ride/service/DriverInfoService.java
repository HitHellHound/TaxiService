package com.efcon.ride.service;

import com.efcon.ride.model.DriverInfo;
import com.efcon.ride.model.DriverStatus;

import java.util.List;

public interface DriverInfoService {
    DriverInfo get(Long id);
    List<Long> getSomeFreeDriverIds(int maxNumber);
    void save(DriverInfo driverInfo);
    void attachCar(Long id, Long carId);
    void detachCar(Long id);
    void changeDriverStatus(Long id, DriverStatus status);
    void delete(Long id);
}
