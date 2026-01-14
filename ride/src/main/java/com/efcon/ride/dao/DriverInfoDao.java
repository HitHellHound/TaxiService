package com.efcon.ride.dao;

import com.efcon.ride.model.DriverInfo;
import com.efcon.ride.model.DriverStatus;

import java.util.Optional;

public interface DriverInfoDao {
    Optional<DriverInfo> get(Long id);
    void save(DriverInfo driverInfo);
    int attachCar(Long id, Long carId);
    int detachCar(Long id);
    int changeDriverStatus(Long id, DriverStatus status);
    void delete(Long id);
}
