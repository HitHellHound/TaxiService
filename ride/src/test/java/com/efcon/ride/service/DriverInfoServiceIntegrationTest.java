package com.efcon.ride.service;

import com.efcon.ride.AbstractIntegrationTest;
import com.efcon.ride.exception.EntityNotFoundException;
import com.efcon.ride.exception.EntityNotUpdatedException;
import com.efcon.ride.model.DriverInfo;
import com.efcon.ride.model.DriverStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
@ResourceLock("DRIVER_INFO_TABLE")
public class DriverInfoServiceIntegrationTest extends AbstractIntegrationTest {
    private final Long nonExistingId = Long.MAX_VALUE;

    @Autowired
    private DriverInfoService driverInfoService;

    @Test
    void shouldSaveAndMakeAccessibleDriverInfo() {
        var driver = createTestFreeDriverOnCar();
        driverInfoService.save(driver);

        var result = driverInfoService.get(driver.getId());

        assertThat(result)
                .extracting(DriverInfo::getId, DriverInfo::getCarId, DriverInfo::getStatus)
                .containsExactly(driver.getId(), driver.getCarId(), driver.getStatus());
    }

    @Test
    void getThrowEntityNotFoundException() {
        assertThatThrownBy(() -> driverInfoService.get(nonExistingId))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void shouldGetOnlyFreeDriverOnCars() {
        var drivers = createTestDrivers();
        var driverIds = drivers.stream()
                .map(DriverInfo::getId)
                .collect(Collectors.toSet());
        drivers.forEach(driverInfoService::save);
        var freeDriversOnCarsIds = drivers.stream()
                .filter(driverInfo -> driverInfo.getCarId() != null && driverInfo.getStatus() == DriverStatus.FREE)
                .map(DriverInfo::getId)
                .toList();

        var result = driverInfoService.getSomeFreeDriverIds(100);

        assertThat(result)
                .isNotNull()
                .filteredOn(driverIds::contains)
                .containsExactlyInAnyOrderElementsOf(freeDriversOnCarsIds);
    }

    @Test
    void shouldGetLimitedNumberFreeDriverOnCars() {
        var drivers = createTestDrivers();
        var driverIds = drivers.stream()
                .map(DriverInfo::getId)
                .collect(Collectors.toSet());
        drivers.forEach(driverInfoService::save);
        var freeDriversOnCarsIds = drivers.stream()
                .filter(driverInfo -> driverInfo.getCarId() != null && driverInfo.getStatus() == DriverStatus.FREE)
                .map(DriverInfo::getId)
                .toList();

        var result = driverInfoService.getSomeFreeDriverIds(freeDriversOnCarsIds.size() - 1);

        assertThat(result)
                .isNotNull()
                .filteredOn(driverIds::contains)
                .containsAnyElementsOf(freeDriversOnCarsIds)
                .size()
                .isEqualTo(freeDriversOnCarsIds.size() - 1);
    }

    @Test
    void shouldAttachCar() {
        Random random = new Random();
        var driver = new DriverInfo(random.nextLong(1, nonExistingId - 1), null, DriverStatus.FREE);
        driverInfoService.save(driver);

        var carId = random.nextLong(1, Long.MAX_VALUE);
        driverInfoService.attachCar(driver.getId(), carId);

        assertThat(driverInfoService.get(driver.getId()))
                .extracting(DriverInfo::getCarId)
                .isEqualTo(carId);
    }

    @Test
    void throwEntityNotUpdatedExceptionIfCarNotAttached() {
        Random random = new Random();

        var carId = random.nextLong(1, Long.MAX_VALUE);
        assertThatThrownBy(() -> driverInfoService.attachCar(nonExistingId, carId))
                .isInstanceOf(EntityNotUpdatedException.class);
    }

    @Test
    void shouldDetachCar() {
        Random random = new Random();
        var driver = new DriverInfo(random.nextLong(1, nonExistingId - 1),
                random.nextLong(1, Long.MAX_VALUE), DriverStatus.FREE);
        driverInfoService.save(driver);

        driverInfoService.detachCar(driver.getId());

        assertThat(driverInfoService.get(driver.getId()))
                .extracting(DriverInfo::getCarId)
                .isNull();
    }

    @Test
    void throwEntityNotUpdatedExceptionIfCarNotDetached() {
        assertThatThrownBy(() -> driverInfoService.detachCar(nonExistingId))
                .isInstanceOf(EntityNotUpdatedException.class);
    }

    @ParameterizedTest
    @MethodSource("allStatusTransitions")
    void shouldChangeDriverStatus(DriverStatus from, DriverStatus to) {
        Random random = new Random();
        var driver = new DriverInfo(random.nextLong(1, nonExistingId - 1),
                random.nextLong(1, Long.MAX_VALUE), from);
        driverInfoService.save(driver);

        driverInfoService.changeDriverStatus(driver.getId(), to);

        assertThat(driverInfoService.get(driver.getId()))
                .extracting(DriverInfo::getStatus)
                .isEqualTo(to);
    }

    @ParameterizedTest
    @EnumSource(DriverStatus.class)
    void throwEntityNotUpdatedExceptionIfStatusNotChanged(DriverStatus driverStatus) {
        assertThatThrownBy(() -> driverInfoService.changeDriverStatus(nonExistingId, driverStatus))
                .isInstanceOf(EntityNotUpdatedException.class);
    }

    static private Stream<Arguments> allStatusTransitions() {
        return Arrays.stream(DriverStatus.values())
                .flatMap(from ->
                        Arrays.stream(DriverStatus.values())
                                .map(to -> Arguments.of(from, to))
                );
    }

    @Test
    void deleteShouldMakeDriverInfoInaccessible() {
        var driver = createTestFreeDriverOnCar();
        driverInfoService.save(driver);

        driverInfoService.delete(driver.getId());

        assertThatThrownBy(() -> driverInfoService.get(driver.getId()))
                .isInstanceOf(EntityNotFoundException.class);
    }

    private DriverInfo createTestFreeDriverOnCar() {
        Random random = new Random();
        return new DriverInfo(random.nextLong(1, nonExistingId - 1),
                random.nextLong(1, Long.MAX_VALUE), DriverStatus.FREE);
    }

    private List<DriverInfo> createTestDrivers() {
        Random random = new Random();
        var driver1 = createTestFreeDriverOnCar();
        var driver2 = new DriverInfo(random.nextLong(1,nonExistingId - 1),
                null, DriverStatus.FREE);
        var driver3 = new DriverInfo(random.nextLong(1, nonExistingId - 1),
                random.nextLong(1, Long.MAX_VALUE), DriverStatus.ON_TRIP);
        var driver4 = new DriverInfo(random.nextLong(1, nonExistingId - 1),
                null, DriverStatus.ON_TRIP);
        var driver5 = createTestFreeDriverOnCar();

        return List.of(driver1, driver2, driver3, driver4, driver5);
    }
}
