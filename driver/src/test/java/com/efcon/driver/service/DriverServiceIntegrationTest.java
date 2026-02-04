package com.efcon.driver.service;

import com.efcon.driver.AbstractIntegrationTest;
import com.efcon.driver.dto.*;
import com.efcon.driver.event.*;
import com.efcon.driver.exception.EntityNotFoundException;
import net.datafaker.Faker;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.assertj.core.api.ThrowingConsumer;
import org.awaitility.core.ThrowingRunnable;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

public class DriverServiceIntegrationTest extends AbstractIntegrationTest {
    @Autowired
    private DriverService driverService;

    @Autowired
    private CarService carService;

    @Autowired
    private ConsumerFactory<String, Object> consumerFactory;

    @Value("${message-broker.kafka.topic.driver-events}")
    private String driverEventTopicName;

    private final Faker faker = new Faker();
    private final long nonExistentId = Long.MAX_VALUE;

    @Test
    void createShouldReturnDriverWithIdAndSameDataAndPostEvent() {
        var name = "John Doe";
        var email = "john.doe@test.org";
        var phoneNumber = "+375448888888";
        var newDriverRequest = new DriverRequest(name, email, phoneNumber);

        var consumer = createDriverEventsTestConsumer();

        var result = driverService.create(newDriverRequest);

        assertThat(result)
                .returns(null, DriverResponse::car)
                .extracting(DriverResponse::id, DriverResponse::name, DriverResponse::email, DriverResponse::phone)
                .doesNotContainNull()
                .containsExactly(result.id(), name, email, phoneNumber);

        await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(
                        createDriverEventAwait(result.id(), consumer, DriverCreatedEvent.class,
                                driverCreatedEvent -> {
                                    assertThat(driverCreatedEvent.driverId()).isEqualTo(result.id());
                                    assertThat(driverCreatedEvent.driverInfo())
                                            .isNotNull()
                                            .extracting(DriverInfo::id, DriverInfo::name, DriverInfo::email,
                                                    DriverInfo::phone)
                                            .containsExactly(result.id(), name, email,
                                                    phoneNumber);
                                })
                );
    }

    @Test
    void shouldGetExistingDriver() {
        var driver = driverService.create(createTestDriver());

        var result = driverService.get(driver.id());

        assertThat(result).isEqualTo(driver);
    }

    @Test
    void throwEntityNotFoundExceptionForNonExistingDriver() {
        assertThatThrownBy(() -> driverService.get(nonExistentId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Driver with id " + nonExistentId + " not found");
    }

    @Test
    void shouldGetAllExistingDrivers() {
        var driver1 = driverService.create(createTestDriver());
        var driver2 = driverService.create(createTestDriver());
        var driver3 = driverService.create(createTestDriver());

        var ids = Stream.of(driver1, driver2, driver3).map(DriverResponse::id).collect(Collectors.toSet());

        var result = driverService.getAll();

        assertThat(result)
                .isNotEmpty()
                .filteredOn(driver -> ids.contains(driver.id()))
                .extracting(DriverResponse::id)
                .containsExactlyInAnyOrderElementsOf(ids);
    }

    @Test
    void updateShouldChangeDriverAndPostEvent() {
        var driver = driverService.create(createTestDriver());
        var newDriverData = createTestDriver();
        var consumer = createDriverEventsTestConsumer();

        var updatedDriver = driverService.update(driver.id(), newDriverData);

        assertThat(updatedDriver)
                .extracting(DriverResponse::id, DriverResponse::name, DriverResponse::email, DriverResponse::phone)
                .containsExactly(driver.id(), newDriverData.name(), newDriverData.email(), newDriverData.phone());

        assertThat(driverService.get(driver.id()))
                .isEqualTo(updatedDriver);

        await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(
                        createDriverEventAwait(driver.id(), consumer, DriverChangedEvent.class,
                                driverChangedEvent -> {
                                    assertThat(driverChangedEvent.driverId()).isEqualTo(driver.id());
                                    assertThat(driverChangedEvent.driverInfo())
                                            .isNotNull()
                                            .extracting(DriverInfo::id, DriverInfo::name, DriverInfo::email,
                                                    DriverInfo::phone)
                                            .containsExactly(driver.id(), newDriverData.name(), newDriverData.email(),
                                                    newDriverData.phone());
                                })
                );
    }

    @Test
    void updateThrowEntityNotFoundExceptionForNonExistingDriver() {
        var newDriverData = createTestDriver();
        assertThatThrownBy(() -> driverService.update(nonExistentId, newDriverData))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Driver with id " + nonExistentId + " not found");
    }

    @Test
    void deleteShouldMakeDriverInaccessibleAndPostEvent() {
        var driver = driverService.create(createTestDriver());

        var consumer = createDriverEventsTestConsumer();

        driverService.delete(driver.id());

        assertThatThrownBy(() -> driverService.get(driver.id()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Driver with id " + driver.id() + " not found");

        assertThat(driverService.getAll())
                .filteredOn(d -> Objects.equals(d.id(), driver.id()))
                .isEmpty();

        await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(
                        createDriverEventAwait(driver.id(), consumer, DriverDeletedEvent.class,
                                driverChangedEvent ->
                                        assertThat(driverChangedEvent.driverId()).isEqualTo(driver.id()))
                );
    }

    @Test
    void shouldAttachCarAndPostEvent() {
        var driver = driverService.create(createTestDriver());
        var car = carService.create(new CarRequest("1111AT-7", "color", "brand"));

        var consumer = createDriverEventsTestConsumer();

        var driverWithCar = driverService.attachCar(driver.id(), car.id());

        assertThat(driverWithCar)
                .isNotNull()
                .satisfies(driverResponse -> {
                    assertThat(driverResponse.id()).isEqualTo(driver.id());
                    assertThat(driverResponse.car())
                            .extracting(CarResponse::id, CarResponse::number, CarResponse::color, CarResponse::brand)
                            .containsExactly(car.id(), car.number(), car.color(), car.brand());
                });

        assertThat(driverService.get(driver.id()))
                .isEqualTo(driverWithCar);

        await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(
                        createDriverEventAwait(driver.id(), consumer, DriverShiftStartedEvent.class,
                                driverShiftStartedEvent -> {
                                    assertThat(driverShiftStartedEvent.driverId()).isEqualTo(driver.id());
                                    assertThat(driverShiftStartedEvent.carId()).isEqualTo(car.id());
                                }
                        ));
    }

    @Test
    void attachThrowEntityNotFoundExceptionForNonExistingDriver() {
        var car = carService.create(new CarRequest("1111AT-7", "color", "brand"));

        assertThatThrownBy(() -> driverService.attachCar(nonExistentId, car.id()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Driver with id " + nonExistentId + " not found");
    }

    @Test
    void attachThrowEntityNotFoundExceptionForNonExistingCar() {
        var driver = driverService.create(createTestDriver());

        assertThatThrownBy(() -> driverService.attachCar(driver.id(), nonExistentId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Car with id " + nonExistentId + " not found");
    }

    @Test
    void shouldDetachCarAndPostEvent() {
        var driver = driverService.create(createTestDriver());
        var car = carService.create(new CarRequest("1111AT-5", "color", "brand"));
        driverService.attachCar(driver.id(), car.id());

        var consumer = createDriverEventsTestConsumer();

        driverService.detachCar(driver.id());

        assertThat(driverService.get(driver.id()))
                .isNotNull()
                .satisfies(driverResponse -> {
                    assertThat(driverResponse.id()).isEqualTo(driver.id());
                    assertThat(driverResponse.car()).isNull();
                });

        await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(
                        createDriverEventAwait(driver.id(), consumer, DriverShiftEndedEvent.class,
                                driverShiftEndedEvent ->
                                        assertThat(driverShiftEndedEvent.driverId()).isEqualTo(driver.id())
                        ));
    }

    @Test
    void detachThrowEntityNotFoundExceptionForNonExistingDriver() {
        assertThatThrownBy(() -> driverService.detachCar(nonExistentId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Driver with id " + nonExistentId + " not found");
    }

    private DriverRequest createTestDriver() {
        var uuid = UUID.randomUUID();
        var name = "John Doe #" + uuid;
        var email = "john.doe." + uuid + "@test.org";
        var phoneNumber = faker.regexify("\\+375(25|29|33|44)\\d{7}");
        return new DriverRequest(name, email, phoneNumber);
    }

    private Consumer<String, Object> createDriverEventsTestConsumer() {
        var testName = StackWalker.getInstance()
                .walk(frames -> frames.skip(1).findFirst())
                .map(StackWalker.StackFrame::getMethodName)
                .orElse("unknownDriverEventTest");
        var uuid = testName + '-' +  UUID.randomUUID();
        var groupId = "test-group-" + uuid;
        var consumer = consumerFactory.createConsumer(groupId, uuid);
        consumer.subscribe(List.of(driverEventTopicName));
        consumer.poll(Duration.ZERO);
        return consumer;
    }

    private <T> ThrowingRunnable createDriverEventAwait(Long driverId, Consumer<String, Object> consumer,
                                                        Class<T> eventType, ThrowingConsumer<T> conditionOfSatisfaction) {
        return () -> {
            ConsumerRecords<String, Object> records = KafkaTestUtils.getRecords(consumer, Duration.ofMillis(500));

            var driverDeletedEvent = StreamSupport.stream(records.spliterator(), false)
                    .filter(record -> record.key() != null && record.key().equals(driverId.toString()))
                    .filter(record -> eventType.isInstance(record.value()))
                    .map(record -> eventType.cast(record.value()))
                    .findAny();

            assertThat(driverDeletedEvent)
                    .isPresent()
                    .get()
                    .satisfies(conditionOfSatisfaction);
        };
    }
}
