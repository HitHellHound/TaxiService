package com.efcon.ride.service;

import com.efcon.ride.AbstractIntegrationTest;
import com.efcon.ride.dao.RideNotificationDao;
import com.efcon.ride.dto.RideInfo;
import com.efcon.ride.dto.RideNotification;
import com.efcon.ride.model.DriverInfo;
import com.efcon.ride.model.DriverStatus;
import net.datafaker.Faker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.awaitility.Awaitility.await;

@ResourceLock("RIDE_NOTIFICATION")
@Execution(ExecutionMode.SAME_THREAD)
public class RideNotificationServiceIntegrationTest extends AbstractIntegrationTest {
    private final Faker faker = new Faker();

    @Autowired
    private RideNotificationDao rideNotificationDao;

    @Autowired
    private RideNotificationService rideNotificationService;

    @Autowired
    private DriverInfoService driverInfoService;

    @Autowired
    private RabbitAdmin rabbitAdmin;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    @Qualifier("rideNotificationExchange")
    private FanoutExchange rideNotificationExchange;

    @Value("${mq.ride-notifications.resend.stale-after.seconds}")
    private Integer notificationStaleAfter;

    @Autowired
    private JdbcClient jdbcClient;

    @Test
    void shouldOpenRideNotificationAndPostIt() {
        var drivers = createTestDrivers();
        drivers.forEach(driverInfoService::save);
        var freeDriversOnCars = drivers.stream()
                .filter(driverInfo -> driverInfo.getCarId() != null && driverInfo.getStatus() == DriverStatus.FREE)
                .map(DriverInfo::getId)
                .toList();

        var ride = createTestRideInfo();
        var testQueue = createTestQueue();

        rideNotificationService.notifyDrivers(ride);

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

        await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    var notifications = getAllNotificationsFromQueue(testQueue);
                    assertThat(notifications)
                            .filteredOn(rideNotification ->
                                    rideNotification.rideInfo() != null &&
                                            Objects.equals(rideNotification.rideInfo().rideId(), ride.rideId()))
                            .first()
                            .isNotNull()
                            .satisfies(rideNotification ->
                                    assertThat(rideNotification.driverIds())
                                            .isNotNull()
                                            .isNotEmpty()
                                            .containsAll(freeDriversOnCars)
                            )
                            .extracting(RideNotification::rideInfo)
                            .returns(ride.rideId(), RideInfo::rideId)
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
    void shouldCloseRideNotification() {
        var ride = createTestRideInfo();

        rideNotificationService.notifyDrivers(ride);

        await().atMost(1, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    assertThat(rideNotificationDao.findStaleOpenedNotifications(0))
                            .filteredOn(rideInfo -> Objects.equals(rideInfo.rideId(), ride.rideId()))
                            .isNotEmpty();
                });

        rideNotificationService.closeNotification(ride.rideId());

        await().atMost(1, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    assertThat(rideNotificationDao.findStaleOpenedNotifications(0))
                            .filteredOn(rideInfo -> Objects.equals(rideInfo.rideId(), ride.rideId()))
                            .isEmpty();
                });
    }

    @Test
    void shouldPostStaleNotifications() {
        var ride = createTestRideInfo();
        rideNotificationService.notifyDrivers(ride);

        var testQueue = createTestQueue();
        var drivers = createTestDrivers();
        drivers.forEach(driverInfoService::save);
        var freeDriversOnCars = drivers.stream()
                .filter(driverInfo -> driverInfo.getCarId() != null && driverInfo.getStatus() == DriverStatus.FREE)
                .map(DriverInfo::getId)
                .toList();

        await().pollDelay(notificationStaleAfter + 1, TimeUnit.SECONDS)
                .untilAsserted(() -> rideNotificationService.resendStaleNotifications());

        await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    var notifications = getAllNotificationsFromQueue(testQueue);

                    assertThat(notifications)
                            .filteredOn(rideNotification ->
                                    rideNotification.rideInfo() != null &&
                                            Objects.equals(rideNotification.rideInfo().rideId(), ride.rideId()))
                            .first()
                            .isNotNull()
                            .satisfies(rideNotification ->
                                    assertThat(rideNotification.driverIds())
                                            .isNotNull()
                                            .isNotEmpty()
                                            .containsAll(freeDriversOnCars)
                            )
                            .extracting(RideNotification::rideInfo)
                            .returns(ride.rideId(), RideInfo::rideId)
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
    void shouldMakeMakeNotificationsFresh() {
        var ride = createTestRideInfo();
        rideNotificationService.notifyDrivers(ride);

        var drivers = createTestDrivers();
        drivers.forEach(driverInfoService::save);
        var freeDriversOnCars = drivers.stream()
                .filter(driverInfo -> driverInfo.getCarId() != null && driverInfo.getStatus() == DriverStatus.FREE)
                .map(DriverInfo::getId)
                .toList();

        await().pollDelay(notificationStaleAfter + 1, TimeUnit.SECONDS)
                .untilAsserted(() -> rideNotificationService.resendStaleNotifications());

        var testQueue = createTestQueue();

        await().pollDelay(notificationStaleAfter + 1, TimeUnit.SECONDS)
                .untilAsserted(() -> rideNotificationService.resendStaleNotifications());

        await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    var notifications = getAllNotificationsFromQueue(testQueue);

                    assertThat(notifications)
                            .filteredOn(rideNotification ->
                                    rideNotification.rideInfo() != null &&
                                            Objects.equals(rideNotification.rideInfo().rideId(), ride.rideId()))
                            .first()
                            .isNotNull()
                            .satisfies(rideNotification ->
                                    assertThat(rideNotification.driverIds())
                                            .isNotNull()
                                            .isNotEmpty()
                                            .containsAll(freeDriversOnCars)
                            )
                            .extracting(RideNotification::rideInfo)
                            .returns(ride.rideId(), RideInfo::rideId)
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

    private DriverInfo createTestFreeDriverOnCar() {
        Random random = new Random();
        return new DriverInfo(random.nextLong(1, Long.MAX_VALUE),
                random.nextLong(1, Long.MAX_VALUE), DriverStatus.FREE);
    }

    private List<DriverInfo> createTestDrivers() {
        Random random = new Random();
        var driver1 = createTestFreeDriverOnCar();
        var driver2 = new DriverInfo(random.nextLong(1, Long.MAX_VALUE),
                null, DriverStatus.FREE);
        var driver3 = new DriverInfo(random.nextLong(1, Long.MAX_VALUE),
                random.nextLong(1, Long.MAX_VALUE), DriverStatus.ON_TRIP);
        var driver4 = new DriverInfo(random.nextLong(1, Long.MAX_VALUE),
                null, DriverStatus.ON_TRIP);
        var driver5 = createTestFreeDriverOnCar();

        return List.of(driver1, driver2, driver3, driver4, driver5);
    }

    private Queue createTestQueue() {
        var testName = StackWalker.getInstance()
                .walk(frames -> frames.skip(1).findFirst())
                .map(StackWalker.StackFrame::getMethodName)
                .orElse("unknownRideNotificationServiceTest");
        String queueName = testName + UUID.randomUUID();

        Queue testQueue = new Queue(queueName, false, true, true);
        rabbitAdmin.declareQueue(testQueue);

        Binding binding = BindingBuilder.bind(testQueue).to(rideNotificationExchange);
        rabbitAdmin.declareBinding(binding);

        rabbitTemplate.execute(channel -> channel.queueDeclarePassive(queueName));

        return testQueue;
    }

    private List<RideNotification> getAllNotificationsFromQueue(Queue queue) {
        List<RideNotification> notifications = new ArrayList<>();
        while (true) {
            Object msg = rabbitTemplate.receiveAndConvert(queue.getName());
            if (msg == null)
                break;
            if (msg instanceof RideNotification rideNotification) {
                notifications.add(rideNotification);
            }
        }
        return notifications;
    }
}
