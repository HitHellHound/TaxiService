package com.efcon.ride.external.event;

import com.efcon.ride.AbstractIntegrationTest;
import com.efcon.ride.dto.OutboundDriverInfo;
import com.efcon.ride.exception.EntityNotFoundException;
import com.efcon.ride.model.DriverInfo;
import com.efcon.ride.model.DriverStatus;
import com.efcon.ride.service.DriverInfoService;
import net.datafaker.Faker;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

@ResourceLock("DRIVER_INFO_TABLE")
class DriverOutboundKafkaEventListenerTest extends AbstractIntegrationTest {
    private static JdbcClient jdbcClient;

    @Autowired
    private DriverInfoService driverInfoService;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${outbound.events.kafka.topic.driver-events}")
    private String driverEventTopic;

    private final Faker faker = new Faker();

    @Test
    void shouldIdempotentCreateDriverInfo() {
        var driver = createTestOutboundDriver();
        var driverCreatedEvent = new DriverCreatedEvent(driver.id(), driver);

        kafkaTemplate.send(driverEventTopic, driver.id().toString(), driverCreatedEvent);
        kafkaTemplate.send(driverEventTopic, driver.id().toString(), driverCreatedEvent);

        await().atMost(10, TimeUnit.SECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .ignoreException(EntityNotFoundException.class)
                .untilAsserted(() -> {
                    var driverInfo = driverInfoService.get(driver.id());
                    assertThat(driverInfo)
                            .isNotNull()
                            .extracting(DriverInfo::getId, DriverInfo::getCarId, DriverInfo::getStatus)
                            .containsExactly(driver.id(), null, DriverStatus.FREE);
                });
    }

    @Test
    void shouldIdempotentAttachCarWhenShiftStarted() {
        var driverId = new Random().nextLong(1, Long.MAX_VALUE);
        driverInfoService.save(new DriverInfo(driverId, null, DriverStatus.FREE));

        var carId = new Random().nextLong(1, Long.MAX_VALUE);
        var driverShiftStartedEvent = new DriverShiftStartedEvent(driverId, carId);

        kafkaTemplate.send(driverEventTopic, Long.toString(driverId), driverShiftStartedEvent);
        kafkaTemplate.send(driverEventTopic, Long.toString(driverId), driverShiftStartedEvent);

        await().atMost(10, TimeUnit.SECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .ignoreException(EntityNotFoundException.class)
                .untilAsserted(() -> {
                    var driverInfo = driverInfoService.get(driverId);
                    assertThat(driverInfo)
                            .isNotNull()
                            .extracting(DriverInfo::getId, DriverInfo::getCarId)
                            .containsExactly(driverId, carId);
                });
    }

    @ParameterizedTest
    @EnumSource(DriverStatus.class)
    void shouldIdempotentDetachCarWhenShiftEnded(DriverStatus driverStatus) {
        var driverId = new Random().nextLong(1, Long.MAX_VALUE);
        var carId = new Random().nextLong(1, Long.MAX_VALUE);
        driverInfoService.save(new DriverInfo(driverId, carId, driverStatus));

        var driverShiftEndedEvent = new DriverShiftEndedEvent(driverId);

        kafkaTemplate.send(driverEventTopic, Long.toString(driverId), driverShiftEndedEvent);
        kafkaTemplate.send(driverEventTopic, Long.toString(driverId), driverShiftEndedEvent);

        await().atMost(10, TimeUnit.SECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .ignoreException(EntityNotFoundException.class)
                .untilAsserted(() -> {
                    var driverInfo = driverInfoService.get(driverId);
                    assertThat(driverInfo)
                            .isNotNull()
                            .extracting(DriverInfo::getId, DriverInfo::getCarId)
                            .containsExactly(driverId, null);
                });
    }

    @ParameterizedTest
    @EnumSource(DriverStatus.class)
    void shouldIdempotentMakeDriverInfoInaccessibleWhenDeleted(DriverStatus driverStatus) {
        var driverId = new Random().nextLong(1, Long.MAX_VALUE);
        var carId = new Random().nextLong(1, Long.MAX_VALUE);
        driverInfoService.save(new DriverInfo(driverId, carId, driverStatus));

        var driverDeletedEvent = new DriverDeletedEvent(driverId);

        kafkaTemplate.send(driverEventTopic, Long.toString(driverId), driverDeletedEvent);
        kafkaTemplate.send(driverEventTopic, Long.toString(driverId), driverDeletedEvent);

        await().atMost(10, TimeUnit.SECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    assertThatThrownBy(() -> driverInfoService.get(driverId))
                            .isInstanceOf(EntityNotFoundException.class);
                });
    }

    private OutboundDriverInfo createTestOutboundDriver() {
        var uuid = UUID.randomUUID();
        var driverId = new Random().nextLong(1, Long.MAX_VALUE);
        var name = "John Doe #" + uuid;
        var email = "john.doe." + uuid + "@test.org";
        var phoneNumber = faker.regexify("\\+375(25|29|33|44)\\d{7}");
        return new OutboundDriverInfo(driverId, name, email, phoneNumber);
    }

    @AfterAll
    static void clearDriverInfoTable() {
        jdbcClient.sql("TRUNCATE TABLE driver_info").update();
    }

    @Autowired
    public void setJdbcClient(JdbcClient jdbcClient) {
        DriverOutboundKafkaEventListenerTest.jdbcClient = jdbcClient;
    }
}