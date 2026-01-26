package com.efcon.ride.service;

import com.efcon.ride.dao.DriverInfoDao;
import com.efcon.ride.exception.EntityNotFoundException;
import com.efcon.ride.exception.EntityNotUpdatedException;
import com.efcon.ride.model.DriverInfo;
import com.efcon.ride.model.DriverStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DriverInfoServiceImpl implements DriverInfoService {
    private final DriverInfoDao driverInfoDao;

    @Override
    public DriverInfo get(Long id) {
        return driverInfoDao.get(id)
                .orElseThrow(() -> new EntityNotFoundException("DriverInfo with id " + id + " not found"));
    }

    @Override
    public List<Long> getSomeFreeDriverIds(int maxNumber) {
        return driverInfoDao.getSomeFreeDriverOnCarIds(maxNumber);
    }

    @Override
    public void save(DriverInfo driverInfo) {
        driverInfoDao.save(driverInfo);
    }

    @Override
    public void attachCar(Long id, Long carId) {
        int changesCount = driverInfoDao.attachCar(id, carId);
        if (changesCount == 0) {
            throw new EntityNotUpdatedException("Car with id " + carId + " didn't set to driver with id " + id);
        }
    }

    @Override
    public void detachCar(Long id) {
        int changesCount = driverInfoDao.detachCar(id);
        if (changesCount == 0) {
            throw new EntityNotUpdatedException("Car didn't detached from driver with id " + id);
        }
    }

    @Override
    public void changeDriverStatus(Long id, DriverStatus status) {
        int changesCount = driverInfoDao.changeDriverStatus(id, status);
        if (changesCount == 0) {
            throw new EntityNotUpdatedException("New driver status " + status + " didn't set to driver with id " + id);
        }
    }

    @Override
    public void delete(Long id) {
        driverInfoDao.delete(id);
    }
}
