package com.efcon.ride.dao;

import com.efcon.ride.AbstractIntegrationTest;
import com.efcon.ride.model.DriverInfo;
import com.efcon.ride.model.DriverStatus;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@ResourceLock("DRIVER_INFO_TABLE")
class DriverInfoDaoIntegrationTest extends AbstractIntegrationTest {
    private static final Long nonExistingId = Long.MAX_VALUE;
    @Autowired
    private DriverInfoDao driverInfoDao;

    @Autowired
    private JdbcClient jdbcClient;

    @Test
    void shouldSaveAndMakeAccessibleDriverInfo() {
        var driver = createTestFreeDriverOnCar();
        driverInfoDao.save(driver);

        var result = driverInfoDao.get(driver.getId());

        assertThat(result)
                .isPresent()
                .get()
                .extracting(DriverInfo::getId, DriverInfo::getCarId, DriverInfo::getStatus)
                .containsExactly(driver.getId(), driver.getCarId(), driver.getStatus());
    }

    @Test
    void getShouldReturnEmptyOptionalWhenDriverInfoNotExist() {
        assertThat(driverInfoDao.get(nonExistingId))
                .isEmpty();
    }

    @Test
    void shouldGetOnlyFreeDriverOnCars() {
        var drivers = createTestDrivers();
        var driverIds = drivers.stream()
                .map(DriverInfo::getId)
                .collect(Collectors.toSet());
        drivers.forEach(driverInfoDao::save);
        var freeDriversOnCarsIds = drivers.stream()
                .filter(driverInfo -> driverInfo.getCarId() != null && driverInfo.getStatus() == DriverStatus.FREE)
                .map(DriverInfo::getId)
                .toList();

        var result = driverInfoDao.getSomeFreeDriverOnCarIds(100);

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
        drivers.forEach(driverInfoDao::save);
        var freeDriversOnCarsIds = drivers.stream()
                .filter(driverInfo -> driverInfo.getCarId() != null && driverInfo.getStatus() == DriverStatus.FREE)
                .map(DriverInfo::getId)
                .toList();

        var result = driverInfoDao.getSomeFreeDriverOnCarIds(freeDriversOnCarsIds.size() - 1);

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
        driverInfoDao.save(driver);

        var carId = random.nextLong(1, Long.MAX_VALUE);
        var result = driverInfoDao.attachCar(driver.getId(), carId);

        assertThat(result)
                .isEqualTo(1);

        assertThat(driverInfoDao.get(driver.getId()))
                .isPresent()
                .get()
                .extracting(DriverInfo::getCarId)
                .isEqualTo(carId);
    }

    @Test
    void shouldReturnZeroIfCarNotAttached() {
        Random random = new Random();

        var carId = random.nextLong(1, Long.MAX_VALUE);
        assertThat(driverInfoDao.attachCar(nonExistingId, carId))
                .isEqualTo(0);
    }

    @Test
    void shouldDetachCar() {
        Random random = new Random();
        var driver = new DriverInfo(random.nextLong(1, nonExistingId - 1),
                random.nextLong(1, Long.MAX_VALUE), DriverStatus.FREE);
        driverInfoDao.save(driver);

        var result = driverInfoDao.detachCar(driver.getId());

        assertThat(result)
                .isEqualTo(1);

        assertThat(driverInfoDao.get(driver.getId()))
                .isPresent()
                .get()
                .extracting(DriverInfo::getCarId)
                .isNull();
    }

    @Test
    void shouldReturnZeroIfCarNotDetached() {
        assertThat(driverInfoDao.detachCar(nonExistingId))
                .isEqualTo(0);
    }

    @ParameterizedTest
    @MethodSource("allStatusTransitions")
    void shouldChangeDriverStatus(DriverStatus from, DriverStatus to) {
        Random random = new Random();
        var driver = new DriverInfo(random.nextLong(1, nonExistingId - 1),
                random.nextLong(1, Long.MAX_VALUE), from);
        driverInfoDao.save(driver);

        var result = driverInfoDao.changeDriverStatus(driver.getId(), to);

        assertThat(result)
                .isEqualTo(1);

        assertThat(driverInfoDao.get(driver.getId()))
                .isPresent()
                .get()
                .extracting(DriverInfo::getStatus)
                .isEqualTo(to);
    }

    @ParameterizedTest
    @EnumSource(DriverStatus.class)
    void throwEntityNotUpdatedExceptionIfStatusNotChanged(DriverStatus driverStatus) {
        assertThat(driverInfoDao.changeDriverStatus(nonExistingId, driverStatus))
                .isEqualTo(0);
    }

    @Test
    void shouldSoftDeleteDriverInfo() {
        var driver = createTestFreeDriverOnCar();
        driverInfoDao.save(driver);

        driverInfoDao.delete(driver.getId());

        assertThat(driverInfoDao.get(driver.getId())).isEmpty();

        var deletedDriver = jdbcClient.sql("SELECT * FROM driver_info WHERE id = :id")
                .param("id", driver.getId())
                .query(DriverInfo.class)
                .single();

        assertThat(deletedDriver)
                .isNotNull()
                .satisfies(d -> {
                    assertThat(d.getId()).isEqualTo(driver.getId());
                });
    }

    @Test
    void getSomeFreeDriverOnCarIdsShouldNotReturnSoftDeletedCars() {
        var driver1 = createTestFreeDriverOnCar();
        var driver2 = createTestFreeDriverOnCar();
        var driver3 = createTestFreeDriverOnCar();
        var drivers = List.of(driver1, driver2, driver3);

        drivers.forEach(driverInfoDao::save);

        var ids = drivers.stream().map(DriverInfo::getId).toList();

        driverInfoDao.delete(driver1.getId());

        var result = driverInfoDao.getSomeFreeDriverOnCarIds(10);

        assertThat(result)
                .filteredOn(ids::contains)
                .containsExactlyInAnyOrder(driver2.getId(), driver3.getId());
    }

    @Test
    void deleteShouldBeIdempotent() {
        var driver = createTestFreeDriverOnCar();
        driverInfoDao.save(driver);

        driverInfoDao.delete(driver.getId());
        driverInfoDao.delete(driver.getId());

        assertThat(driverInfoDao.get(driver.getId()))
                .isEmpty();
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

    static private Stream<Arguments> allStatusTransitions() {
        return Arrays.stream(DriverStatus.values())
                .flatMap(from ->
                        Arrays.stream(DriverStatus.values())
                                .map(to -> Arguments.of(from, to))
                );
    }
}