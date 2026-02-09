package com.efcon.ride.dao;

import com.efcon.ride.AbstractIntegrationTest;
import com.efcon.ride.dto.RideInfo;
import net.datafaker.Faker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.awaitility.Awaitility.await;

@ResourceLock("RIDE_NOTIFICATION")
@Execution(ExecutionMode.SAME_THREAD)
class RideNotificationDaoIntegrationTest extends AbstractIntegrationTest {
    private final Faker faker = new Faker();

    @Autowired
    private RideNotificationDao rideNotificationDao;

    @Autowired
    private JdbcClient jdbcClient;

    @Test
    void createShouldOpenRideNotification() {
        var ride = createTestRideInfo();

        rideNotificationDao.create(ride);

        await().atMost(1, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    assertThat(rideNotificationDao.findStaleOpenedNotifications(0))
                            .filteredOn(rideInfo -> Objects.equals(rideInfo.rideId(), ride.rideId()))
                            .isNotEmpty()
                            .first()
                            .satisfies(rideInfo ->
                                    assertThat(rideInfo.createdAt())
                                            .isCloseTo(ride.createdAt(), within(1, ChronoUnit.SECONDS))
                            )
                            .extracting(RideInfo::rideId, RideInfo::passengerId, RideInfo::startAddress,
                                    RideInfo::destinationAddress, RideInfo::price)
                            .doesNotContainNull()
                            .containsExactly(ride.rideId(), ride.passengerId(), ride.startAddress(),
                                    ride.destinationAddress(), ride.price());
                });
    }

    @Test
    void shouldReturnStaleOpenedNotificationsWithSameData() {
        var notificationStaleAfter = 5;
        var ride = createTestRideInfo();

        rideNotificationDao.create(ride);

        await().pollDelay(notificationStaleAfter + 1, TimeUnit.SECONDS)
                .atMost(notificationStaleAfter + 2, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    var staleNotifications = rideNotificationDao.findStaleOpenedNotifications(notificationStaleAfter);
                    assertThat(staleNotifications)
                            .isNotEmpty()
                            .filteredOn(rideInfo -> Objects.equals(rideInfo.rideId(), ride.rideId()))
                            .isNotEmpty()
                            .first()
                            .satisfies(rideInfo ->
                                    assertThat(rideInfo.createdAt())
                                            .isCloseTo(ride.createdAt(), within(1, ChronoUnit.SECONDS))
                            )
                            .extracting(RideInfo::passengerId, RideInfo::startAddress,
                                    RideInfo::destinationAddress, RideInfo::price)
                            .containsExactly(ride.passengerId(), ride.startAddress(),
                                    ride.destinationAddress(), ride.price());
                });
    }

    @Test
    void shouldCloseRideNotification() {
        var ride = createTestRideInfo();

        rideNotificationDao.create(ride);

        await().atMost(1, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    assertThat(rideNotificationDao.findStaleOpenedNotifications(0))
                            .filteredOn(rideInfo -> Objects.equals(rideInfo.rideId(), ride.rideId()))
                            .isNotEmpty();
                });

        rideNotificationDao.closeNotificationById(ride.rideId());

        await().atMost(1, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    assertThat(rideNotificationDao.findStaleOpenedNotifications(0))
                            .filteredOn(rideInfo -> Objects.equals(rideInfo.rideId(), ride.rideId()))
                            .isEmpty();
                });
    }

    @Test
    void shouldMakeMakeNotificationsFresh() {
        var notificationStaleAfter = 5;
        var ride = createTestRideInfo();

        rideNotificationDao.create(ride);

        await().pollDelay(notificationStaleAfter + 1, TimeUnit.SECONDS)
                .atMost(notificationStaleAfter + 2, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    var staleNotifications = rideNotificationDao.findStaleOpenedNotifications(notificationStaleAfter);
                    assertThat(staleNotifications)
                            .filteredOn(rideInfo -> Objects.equals(rideInfo.rideId(), ride.rideId()))
                            .isNotEmpty();
                    rideNotificationDao.updateLastNotifiedByIds(List.of(ride.rideId()));
                });

        await().atMost(1, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    var staleNotifications = rideNotificationDao.findStaleOpenedNotifications(notificationStaleAfter);
                    assertThat(staleNotifications)
                            .filteredOn(rideInfo -> Objects.equals(rideInfo.rideId(), ride.rideId()))
                            .isEmpty();
                });

        await().pollDelay(notificationStaleAfter + 1, TimeUnit.SECONDS)
                .atMost(notificationStaleAfter + 2, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    var staleNotifications = rideNotificationDao.findStaleOpenedNotifications(notificationStaleAfter);
                    assertThat(staleNotifications)
                            .filteredOn(rideInfo -> Objects.equals(rideInfo.rideId(), ride.rideId()))
                            .isNotEmpty();
                });
    }

    @AfterEach
    void clearRideNotifications() {
        jdbcClient.sql("TRUNCATE TABLE ride_notification").update();
    }

    private RideInfo createTestRideInfo() {
        var random = new Random();
        return new RideInfo(random.nextLong(1, Long.MAX_VALUE), random.nextLong(1, Long.MAX_VALUE),
                faker.address().fullAddress(), faker.address().fullAddress(),
                LocalDateTime.now(), randomInRange(BigDecimal.ONE, BigDecimal.TEN));
    }

    public static BigDecimal randomInRange(BigDecimal min, BigDecimal max) {
        BigDecimal range = max.subtract(min);
        BigDecimal randomFactor = BigDecimal.valueOf(Math.random());
        return min.add(randomFactor.multiply(range)).setScale(2, RoundingMode.UP);
    }
}