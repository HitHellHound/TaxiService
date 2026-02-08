package com.efcon.ride.service;

import com.efcon.ride.AbstractIntegrationTest;
import com.efcon.ride.dto.RideInfo;
import com.efcon.ride.dto.RideRequest;
import com.efcon.ride.dto.RideResponse;
import com.efcon.ride.exception.EntityNotFoundException;
import com.efcon.ride.exception.IllegalRideStatusTransitionException;
import com.efcon.ride.model.DriverInfo;
import com.efcon.ride.model.DriverStatus;
import com.efcon.ride.model.RideStatus;
import net.datafaker.Faker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

@Transactional
public class RideServiceIntegrationTest extends AbstractIntegrationTest {
    private static final Long nonExistingRideId = Long.MAX_VALUE;
    private static final Long maxExistingPassengerId = 9L;
    private final Faker faker = new Faker();

    @Autowired
    private RideService rideService;

    @Autowired
    private DriverInfoService driverInfoService;

    @Test
    void createShouldReturnRideWithIdAndStatusAndSameData() {
        var startAddress = faker.address().fullAddress();
        var destinationAddress = faker.address().fullAddress();
        var price = randomInRange(BigDecimal.ONE, BigDecimal.TEN);
        var rideRequest = new RideRequest(maxExistingPassengerId, startAddress, destinationAddress, price);

        var rideInfoCaptor = ArgumentCaptor.forClass(RideInfo.class);

        var result = rideService.create(rideRequest);

        assertThat(result)
                .isNotNull()
                .returns(null, RideResponse::driverId)
                .satisfies(rideResponse -> {
                    assertThat(rideResponse.createdAt())
                            .isNotNull()
                            .isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS));
                })
                .extracting(RideResponse::id, RideResponse::passengerId, RideResponse::status,
                        RideResponse::startAddress, RideResponse::destinationAddress, RideResponse::price)
                .doesNotContainNull()
                .containsExactly(result.id(), maxExistingPassengerId, RideStatus.CREATED,
                        startAddress, destinationAddress, price);

        Mockito.verify(getRideNotificationServiceSpy()).notifyDrivers(rideInfoCaptor.capture());
        assertThat(rideInfoCaptor.getValue())
                .satisfies(rideInfo ->
                        assertThat(rideInfo.createdAt())
                                .isCloseTo(result.createdAt(), within(1, ChronoUnit.SECONDS))
                )
                .extracting(RideInfo::rideId, RideInfo::passengerId, RideInfo::startAddress,
                        RideInfo::destinationAddress, RideInfo::price)
                .doesNotContainNull()
                .containsExactly(result.id(), result.passengerId(), result.startAddress(),
                        result.destinationAddress(), result.price());
    }

    @Test
    void createThrowExceptionWhenPassengerNotExist() {
        var startAddress = faker.address().fullAddress();
        var destinationAddress = faker.address().fullAddress();
        var price = randomInRange(BigDecimal.ONE, BigDecimal.TEN);
        var rideData = new RideRequest(maxExistingPassengerId + 1, startAddress, destinationAddress, price);

        assertThatThrownBy(() -> rideService.create(rideData))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void shouldGetExistingRide() {
        var ride = rideService.create(createTestRide());

        var result = rideService.get(ride.id());

        assertThat(result)
                .isNotNull()
                .satisfies(rideResponse ->
                        assertThat(rideResponse.createdAt())
                                .isCloseTo(ride.createdAt(), within(1, ChronoUnit.SECONDS))
                )
                .extracting(RideResponse::id, RideResponse::passengerId, RideResponse::driverId, RideResponse::status,
                        RideResponse::startAddress, RideResponse::destinationAddress, RideResponse::price)
                .containsExactly(ride.id(), ride.passengerId(), ride.driverId(), ride.status(),
                        ride.startAddress(), ride.destinationAddress(), ride.price());
    }

    @Test
    void getThrowEntityNotFoundExceptionWhenRideNotExist() {
        assertThatThrownBy(() -> rideService.get(nonExistingRideId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Ride with id " + nonExistingRideId + " not found");
    }

    @Test
    void shouldGetAllExistingRide() {
        var ride1 = rideService.create(createTestRide());
        var ride2 = rideService.create(createTestRide());
        var ride3 = rideService.create(createTestRide());
        var rideIds = Stream.of(ride1, ride2, ride3).map(RideResponse::id).collect(Collectors.toSet());

        var result = rideService.getAll();

        assertThat(result)
                .isNotNull()
                .isNotEmpty()
                .filteredOn(rideResponse -> rideIds.contains(rideResponse.id()))
                .extracting(RideResponse::id)
                .containsAll(rideIds);
    }

    @Test
    void shouldUpdateOnlySomeRideData() {
        var ride = rideService.create(createTestRide());
        var newRideData = createTestRide();

        var result = rideService.update(ride.id(), newRideData);

        assertThat(result)
                .isNotNull()
                .satisfies(rideResponse ->
                        assertThat(rideResponse.createdAt())
                                .isCloseTo(ride.createdAt(), within(1, ChronoUnit.SECONDS))
                )
                .extracting(RideResponse::id, RideResponse::passengerId, RideResponse::driverId, RideResponse::status,
                        RideResponse::startAddress, RideResponse::destinationAddress, RideResponse::price)
                .containsExactly(ride.id(), newRideData.passengerId(), ride.driverId(), ride.status(),
                        newRideData.startAddress(), newRideData.destinationAddress(), newRideData.price());

        assertThat(rideService.get(ride.id()))
                .isNotNull()
                .satisfies(rideResponse ->
                        assertThat(rideResponse.createdAt())
                                .isCloseTo(ride.createdAt(), within(1, ChronoUnit.SECONDS))
                )
                .extracting(RideResponse::id, RideResponse::passengerId, RideResponse::driverId, RideResponse::status,
                        RideResponse::startAddress, RideResponse::destinationAddress, RideResponse::price)
                .containsExactly(ride.id(), newRideData.passengerId(), ride.driverId(), ride.status(),
                        newRideData.startAddress(), newRideData.destinationAddress(), newRideData.price());
    }

    @Test
    void updateThrowEntityNotFoundExceptionWhenRideNotExist() {
        var newRideData = createTestRide();
        assertThatThrownBy(() -> rideService.update(nonExistingRideId, newRideData))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Ride with id " + nonExistingRideId + " not found");
    }

    @Test
    void deleteShouldMakeRideInaccessible() {
        var ride = rideService.create(createTestRide());

        rideService.delete(ride.id());

        assertThatThrownBy(() -> rideService.get(ride.id()))
                .isInstanceOf(EntityNotFoundException.class);

        assertThat(rideService.getAll())
                .filteredOn(rideResponse -> Objects.equals(rideResponse.id(), ride.id()))
                .isEmpty();
    }

    @Test
    void acceptShouldChangeRideAndDriverStatusesWhenDriverIsFreeAndOnCar() {
        var ride = rideService.create(createTestRide());
        var driver = createTestFreeDriverOnCar();
        driverInfoService.save(driver);

        var result = rideService.accept(ride.id(), driver.getId());

        assertThat(result)
                .extracting(RideResponse::status)
                .isEqualTo(RideStatus.ACCEPTED);

        assertThat(rideService.get(ride.id()))
                .extracting(RideResponse::status)
                .isEqualTo(RideStatus.ACCEPTED);

        assertThat(driverInfoService.get(driver.getId()))
                .extracting(DriverInfo::getStatus)
                .isEqualTo(DriverStatus.ON_TRIP);
    }

    @ParameterizedTest
    @EnumSource(DriverStatus.class)
    void acceptThrowIllegalRideStatusTransitionExceptionWhenDriverIsNotOnCar(DriverStatus driverStatus) {
        var ride = rideService.create(createTestRide());
        Random random = new Random();
        var driver = new DriverInfo(random.nextLong(1, Long.MAX_VALUE),
                null, driverStatus);
        driverInfoService.save(driver);

        assertThatThrownBy(() -> rideService.accept(ride.id(), driver.getId()))
                .isInstanceOf(IllegalRideStatusTransitionException.class)
                .hasMessage("Driver with id " + driver.getId() + " doesn't have a car");
    }

    @ParameterizedTest
    @EnumSource(value = DriverStatus.class, names = "FREE", mode = EnumSource.Mode.EXCLUDE)
    void acceptThrowIllegalRideStatusTransitionExceptionWhenDriverIsNotFree(DriverStatus driverStatus) {
        var ride = rideService.create(createTestRide());
        Random random = new Random();
        var driver = new DriverInfo(random.nextLong(1, Long.MAX_VALUE),
                random.nextLong(1, Long.MAX_VALUE), driverStatus);
        driverInfoService.save(driver);

        assertThatThrownBy(() -> rideService.accept(ride.id(), driver.getId()))
                .isInstanceOf(IllegalRideStatusTransitionException.class)
                .hasMessage("Driver with id " + driver.getId() + " already on trip");
    }

    @Test
    void rideFullLifecycleShouldChangeRideStatusOnEachStep() {
        var ride = rideService.create(createTestRide());
        var driver = createTestFreeDriverOnCar();
        driverInfoService.save(driver);

        var result = rideService.accept(ride.id(), driver.getId());
        assertThat(result)
                .extracting(RideResponse::status)
                .isEqualTo(RideStatus.ACCEPTED);
        assertThat(rideService.get(ride.id()))
                .extracting(RideResponse::status)
                .isEqualTo(RideStatus.ACCEPTED);

        result = rideService.driveToPassenger(ride.id());
        assertThat(result)
                .extracting(RideResponse::status)
                .isEqualTo(RideStatus.DRIVING_TO_PASSENGER);
        assertThat(rideService.get(ride.id()))
                .extracting(RideResponse::status)
                .isEqualTo(RideStatus.DRIVING_TO_PASSENGER);

        result = rideService.driveToDestination(ride.id());
        assertThat(result)
                .extracting(RideResponse::status)
                .isEqualTo(RideStatus.DRIVING_TO_DESTINATION);
        assertThat(rideService.get(ride.id()))
                .extracting(RideResponse::status)
                .isEqualTo(RideStatus.DRIVING_TO_DESTINATION);

        result = rideService.complete(ride.id());
        assertThat(result)
                .extracting(RideResponse::status)
                .isEqualTo(RideStatus.COMPLETED);
        assertThat(rideService.get(ride.id()))
                .extracting(RideResponse::status)
                .isEqualTo(RideStatus.COMPLETED);
    }

    @Test
    void completeShouldChangeDriverStatusToFree() {
        var ride = rideService.create(createTestRide());
        var driver = createTestFreeDriverOnCar();
        driverInfoService.save(driver);

        rideService.accept(ride.id(), driver.getId());
        rideService.driveToPassenger(ride.id());
        rideService.driveToDestination(ride.id());
        rideService.complete(ride.id());

        assertThat(driverInfoService.get(driver.getId()))
                .extracting(DriverInfo::getStatus)
                .isEqualTo(DriverStatus.FREE);
    }

    @Test
    void cancelShouldChangeRideStatus() {
        var ride = rideService.create(createTestRide());

        var result = rideService.cancel(ride.id());

        assertThat(result)
                .extracting(RideResponse::status)
                .isEqualTo(RideStatus.CANCELED);

        assertThat(rideService.get(ride.id()))
                .extracting(RideResponse::status)
                .isEqualTo(RideStatus.CANCELED);
    }

    @Test
    void cancelShouldChangeDriverStatusToFreeWhenDriverAssigned() {
        var ride = rideService.create(createTestRide());
        var driver = createTestFreeDriverOnCar();
        driverInfoService.save(driver);

        rideService.accept(ride.id(), driver.getId());
        rideService.cancel(ride.id());

        assertThat(driverInfoService.get(driver.getId()))
                .extracting(DriverInfo::getStatus)
                .isEqualTo(DriverStatus.FREE);
    }

    @Test
    void acceptShouldCloseRideNotification() {
        var ride = rideService.create(createTestRide());
        var driver = createTestFreeDriverOnCar();
        driverInfoService.save(driver);

        rideService.accept(ride.id(), driver.getId());

        Mockito.verify(getRideNotificationServiceSpy())
                .closeNotification(Mockito.argThat(id -> Objects.equals(ride.id(), id)));
    }

    @Test
    void cancelShouldCloseRideNotification() {
        var ride = rideService.create(createTestRide());

        rideService.cancel(ride.id());

        Mockito.verify(getRideNotificationServiceSpy())
                .closeNotification(Mockito.argThat(id -> Objects.equals(ride.id(), id)));
    }

    private DriverInfo createTestFreeDriverOnCar() {
        Random random = new Random();
        return new DriverInfo(random.nextLong(1, Long.MAX_VALUE),
                random.nextLong(1, Long.MAX_VALUE), DriverStatus.FREE);
    }

    private RideRequest createTestRide() {
        var startAddress = faker.address().fullAddress();
        var destinationAddress = faker.address().fullAddress();
        var price = randomInRange(BigDecimal.ONE, BigDecimal.TEN);
        return new RideRequest(maxExistingPassengerId, startAddress, destinationAddress, price);
    }

    public static BigDecimal randomInRange(BigDecimal min, BigDecimal max) {
        BigDecimal range = max.subtract(min);
        BigDecimal randomFactor = BigDecimal.valueOf(Math.random());
        return min.add(randomFactor.multiply(range)).setScale(2, RoundingMode.UP);
    }
}
