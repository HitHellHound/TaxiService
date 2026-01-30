package com.efcon.passenger.repository;

import com.efcon.passenger.AbstractIntegrationTest;
import com.efcon.passenger.model.Passenger;
import net.datafaker.Faker;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration(LiquibaseAutoConfiguration.class)
@ComponentScan(basePackages  = "com.efcon.passenger.repository")
class PassengerRepositoryTest extends AbstractIntegrationTest {
    @Autowired
    private PassengerRepository passengerRepository;

    private final Faker faker = new Faker();
    private long nonExistentId = Long.MAX_VALUE;

    @Test
    void saveShouldReturnPassengerWithNewIdAndSameData() {
        var name = "John Doe";
        var email = "john.doe@test.org";
        var phoneNumber = "+375448888888";
        var newPassenger = new Passenger(name, email, phoneNumber);

        var result = passengerRepository.save(newPassenger);

        assertThat(result)
                .isNotNull()
                .extracting(Passenger::getId, Passenger::getName, Passenger::getEmail, Passenger::getPhone)
                .doesNotContainNull()
                .containsExactly(result.getId(), name, email, phoneNumber);
    }

    @Test
    void shouldReturnPassengerById() {
        var passenger = passengerRepository.save(createTestPassenger());

        var result = passengerRepository.findById(passenger.getId());

        assertThat(result)
                .isNotNull()
                .isPresent()
                .get()
                .usingRecursiveComparison()
                .ignoringFields( "deletedAt")
                .isEqualTo(passenger);
    }

    @Test
    void saveShouldUpdateExistingPassenger() {
        var passenger = passengerRepository.save(createTestPassenger());
        var changedPassenger = createTestPassenger();
        changedPassenger.setId(passenger.getId());

        passengerRepository.save(changedPassenger);

        var updatedPassenger = passengerRepository.findById(passenger.getId());
        assertThat(updatedPassenger)
                .isNotNull()
                .isPresent()
                .get()
                .usingRecursiveComparison()
                .ignoringFields( "deletedAt")
                .isEqualTo(changedPassenger);
    }

    @Test
    void countShouldChangeAfterNewPassengerAdded() {
        var before = passengerRepository.count();

        passengerRepository.save(createTestPassenger());
        passengerRepository.save(createTestPassenger());

        var after = passengerRepository.count();
        assertThat(after).isEqualTo(before + 2);
    }

    @Test
    void shouldThrowExceptionIfCreatePassengerWithExistingEmail() {
        var passenger1 = createTestPassenger();
        passengerRepository.save(passenger1);
        var passenger2 = createTestPassenger();
        passenger2.setEmail(passenger1.getEmail());

        assertThatThrownBy(() -> passengerRepository.save(passenger2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldThrowExceptionIfCreatePassengerWithExistingPhone() {
        var passenger1 = passengerRepository.save(createTestPassenger());
        var passenger2 = createTestPassenger();
        passenger2.setPhone(passenger1.getPhone());

        assertThatThrownBy(() -> passengerRepository.save(passenger2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void findAllShouldReturnSavedPassengers() {
        var passenger1 = passengerRepository.save(createTestPassenger());
        var passenger2 = passengerRepository.save(createTestPassenger());
        var passenger3 = passengerRepository.save(createTestPassenger());

        Set<Long> ids = Stream.of(passenger1, passenger2, passenger3).map(Passenger::getId).collect(Collectors.toSet());

        var result = passengerRepository.findAll();

        assertThat(result)
                .isNotNull()
                .isNotEmpty()
                .filteredOn(passenger -> ids.contains(passenger.getId()))
                .hasSize(3);
    }

    @Test
    void findAllByIdShouldReturnConcretePassengers() {
        var passenger1 = passengerRepository.save(createTestPassenger());
        var passenger2 = passengerRepository.save(createTestPassenger());
        var passenger3 = passengerRepository.save(createTestPassenger());

        Set<Long> ids = Stream.of(passenger1, passenger2, passenger3).map(Passenger::getId).collect(Collectors.toSet());

        var result = passengerRepository.findAllById(ids);

        assertThat(result)
                .isNotNull()
                .hasSize(3)
                .extracting(Passenger::getId)
                .containsExactlyInAnyOrderElementsOf(ids);
    }

    @Test
    void getByIdShouldReturnNothingWhenPassengerNotExists() {
        var result = passengerRepository.findById(nonExistentId);

        assertThat(result)
                .isEmpty();
    }

    @Test
    void existsByIdShouldReturnTrueWhenPassengerExists() {
        var passengerId = passengerRepository.save(createTestPassenger()).getId();

        var result = passengerRepository.existsById(passengerId);

        assertThat(result).isTrue();
    }

    @Test
    void existsByIdShouldReturnFalseWhenPassengerNotExists() {
        var result = passengerRepository.existsById(nonExistentId);

        assertThat(result).isFalse();
    }

    @Test
    void shouldDeletePassengerById() {
        var passengerId = passengerRepository.save(createTestPassenger()).getId();

        passengerRepository.deleteById(passengerId);

        var deletedPassenger = passengerRepository.findById(passengerId);
        assertThat(deletedPassenger)
                .isEmpty();
    }

    @Test
    void shouldDeleteAll() {
        var passenger1 = passengerRepository.save(createTestPassenger());
        var passenger2 = passengerRepository.save(createTestPassenger());
        var passenger3 = passengerRepository.save(createTestPassenger());

        passengerRepository.deleteAll();

        var allPassengers = passengerRepository.findAll();
        assertThat(allPassengers)
                .isEmpty();
    }

    @Test
    void shouldDeleteConcreteByIds() {
        var passenger1 = passengerRepository.save(createTestPassenger());
        var passenger2 = passengerRepository.save(createTestPassenger());
        var passenger3 = passengerRepository.save(createTestPassenger());
        Set<Long> ids = Stream.of(passenger1, passenger2, passenger3).map(Passenger::getId).collect(Collectors.toSet());

        passengerRepository.deleteAllById(ids);

        var passengers = passengerRepository.findAllById(ids);
        assertThat(passengers)
                .isEmpty();
    }

    private Passenger createTestPassenger() {
        var uuid = UUID.randomUUID();
        var name = "John Doe #" + uuid;
        var email = "john.doe." + uuid + "@test.org";
        var phoneNumber = faker.regexify("\\+375(25|29|33|44)\\d{7}");
        return new Passenger(name, email, phoneNumber);
    }
}