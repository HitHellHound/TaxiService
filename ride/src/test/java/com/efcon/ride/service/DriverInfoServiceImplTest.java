package com.efcon.ride.service;

import com.efcon.ride.dao.DriverInfoDao;
import com.efcon.ride.exception.EntityNotFoundException;
import com.efcon.ride.exception.EntityNotUpdatedException;
import com.efcon.ride.model.DriverInfo;
import com.efcon.ride.model.DriverStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class DriverInfoServiceImplTest {
    @Mock
    private DriverInfoDao driverInfoDao;

    @InjectMocks
    private DriverInfoServiceImpl driverInfoService;

    @Test
    void getShouldUseDaoGet() {
        var driverId = 1L;
        var driverInfo = Mockito.mock(DriverInfo.class);
        Mockito.when(driverInfoDao.get(driverId)).thenReturn(Optional.of(driverInfo));

        var result = driverInfoService.get(driverId);

        assertThat(result).isEqualTo(driverInfo);
        Mockito.verify(driverInfoDao).get(driverId);
    }

    @Test
    void getThrowEntityNotFoundException() {
        var driverId = 1L;
        Mockito.when(driverInfoDao.get(driverId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> driverInfoService.get(driverId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("DriverInfo with id " + driverId + " not found");

        Mockito.verify(driverInfoDao).get(driverId);
    }

    @Test
    void getSomeFreeDriverOnCarIdsShouldUseDao() {
        var driver1Id = 1L;
        var driver2Id = 1L;
        var maxNumber = 10;
        var driverIdList = List.of(driver1Id, driver2Id);
        Mockito.when(driverInfoDao.getSomeFreeDriverOnCarIds(maxNumber)).thenReturn(driverIdList);

        var result = driverInfoService.getSomeFreeDriverIds(maxNumber);

        assertThat(result)
                .isNotEmpty()
                .isEqualTo(driverIdList);
        Mockito.verify(driverInfoDao).getSomeFreeDriverOnCarIds(maxNumber);
    }

    @Test
    void saveShouldUseDaoSave() {
        var driverInfo = Mockito.mock(DriverInfo.class);

        driverInfoService.save(driverInfo);

        Mockito.verify(driverInfoDao).save(driverInfo);
    }

    @Test
    void attachCarShouldUseDao() {
        var driverId = 1L;
        var carId = 2L;
        Mockito.when(driverInfoDao.attachCar(driverId, carId)).thenReturn(1);

        driverInfoService.attachCar(driverId, carId);

        Mockito.verify(driverInfoDao).attachCar(driverId, carId);
    }

    @Test
    void attachCarThrowEntityNotUpdatedException() {
        var driverId = 1L;
        var carId = 2L;
        Mockito.when(driverInfoDao.attachCar(driverId, carId)).thenReturn(0);

        assertThatThrownBy(() -> driverInfoService.attachCar(driverId, carId))
                .isInstanceOf(EntityNotUpdatedException.class)
                .hasMessage("Car with id " + carId + " didn't set to driver with id " + driverId);

        Mockito.verify(driverInfoDao).attachCar(driverId, carId);
    }

    @Test
    void detachCarShouldUseDao() {
        var driverId = 1L;
        Mockito.when(driverInfoDao.detachCar(driverId)).thenReturn(1);

        driverInfoService.detachCar(driverId);

        Mockito.verify(driverInfoDao).detachCar(driverId);
    }

    @Test
    void detachCarThrowEntityNotUpdatedException() {
        var driverId = 1L;
        Mockito.when(driverInfoDao.detachCar(driverId)).thenReturn(0);

        assertThatThrownBy(() ->  driverInfoService.detachCar(driverId))
                .isInstanceOf(EntityNotUpdatedException.class)
                .hasMessage("Car didn't detached from driver with id " + driverId);

        Mockito.verify(driverInfoDao).detachCar(driverId);
    }

    @ParameterizedTest
    @EnumSource(value = DriverStatus.class)
    void changeDriverStatusShouldUseDao(DriverStatus driverStatus) {
        var driverId = 1L;
        Mockito.when(driverInfoDao.changeDriverStatus(driverId, driverStatus)).thenReturn(1);

        driverInfoService.changeDriverStatus(driverId, driverStatus);

        Mockito.verify(driverInfoDao).changeDriverStatus(driverId, driverStatus);
    }

    @ParameterizedTest
    @EnumSource(value = DriverStatus.class)
    void changeDriverStatusThrowEntityNotUpdatedException(DriverStatus driverStatus) {
        var driverId = 1L;
        Mockito.when(driverInfoDao.changeDriverStatus(driverId, driverStatus)).thenReturn(0);

        assertThatThrownBy(() ->  driverInfoService.changeDriverStatus(driverId, driverStatus))
                .isInstanceOf(EntityNotUpdatedException.class)
                .hasMessage("New driver status " + driverStatus + " didn't set to driver with id " + driverId);

        Mockito.verify(driverInfoDao).changeDriverStatus(driverId, driverStatus);
    }

    @Test
    void deleteShouldUseDao() {
        var driverId = 1L;

        driverInfoService.delete(driverId);

        Mockito.verify(driverInfoDao).delete(driverId);
    }
}