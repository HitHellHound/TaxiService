package com.efcon.ride.service;

import com.efcon.ride.AbstractIntegrationTest;
import com.efcon.ride.dto.PassengerRequest;
import com.efcon.ride.dto.PassengerResponse;
import com.efcon.ride.exception.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Random;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PassengerServiceIntegrationTest extends AbstractIntegrationTest {
    @Autowired
    private PassengerService passengerService;

    private static final Long maxExistingPassengerId = 9L;

    @ParameterizedTest
    @MethodSource("provideIds")
    void shouldSuccessfullyGetExistingPassenger(Long existingPassengerId) {
        var result = passengerService.get(existingPassengerId);
        assertThat(result)
                .isNotNull()
                .extracting(PassengerResponse::id)
                .isEqualTo(existingPassengerId);
    }

    @Test
    void throwEntityNotFoundExceptionWhenPassengerNotExist() {
        var nonExistingId = new Random().nextLong(maxExistingPassengerId, Long.MAX_VALUE);
        assertThatThrownBy(() -> passengerService.get(nonExistingId))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void shouldGetListOfPassengers() {
        var result = passengerService.getAll();
        assertThat(result)
                .isNotNull()
                .isNotEmpty();
    }

    @Test
    void shouldCreatePassenger() {
        var request = new PassengerRequest("John Doe", "john.doe@mail.com", "+375296667778");

        var result = passengerService.create(request);

        assertThat(result)
                .isNotNull()
                .extracting(PassengerResponse::id, PassengerResponse::name, PassengerResponse::email,
                        PassengerResponse::phone)
                .doesNotContainNull()
                .containsExactly(result.id(), request.name(), request.email(), request.phone());
    }

    @ParameterizedTest
    @MethodSource("provideIds")
    void shouldUpdatePassenger(Long existingPassengerId) {
        var request = new PassengerRequest("John Doe", "john.doe@mail.com", "+375296667778");

        var result = passengerService.update(existingPassengerId, request);

        assertThat(result)
                .isNotNull()
                .extracting(PassengerResponse::id, PassengerResponse::name, PassengerResponse::email,
                        PassengerResponse::phone)
                .doesNotContainNull()
                .containsExactly(result.id(), request.name(), request.email(), request.phone());
    }

    @Test
    void throwEntityNotFoundExceptionWhenUpdatingPassengerNotExist() {
        var request = new PassengerRequest("John Doe", "john.doe@mail.com", "+375296667778");

        assertThatThrownBy(() -> passengerService.update(maxExistingPassengerId + 1, request))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void shouldSuccessfullyDeletePassengerById() {
        passengerService.delete(maxExistingPassengerId);
    }

    private static Stream<Long> provideIds() {
        return LongStream.rangeClosed(1, maxExistingPassengerId).boxed();
    }
}