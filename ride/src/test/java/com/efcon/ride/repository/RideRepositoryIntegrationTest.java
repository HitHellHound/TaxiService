package com.efcon.ride.repository;

import com.efcon.ride.AbstractIntegrationTest;
import com.efcon.ride.model.Ride;
import com.efcon.ride.model.RideStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.ConstraintViolationException;
import net.datafaker.Faker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
class RideRepositoryIntegrationTest extends AbstractIntegrationTest {
    private final Faker faker = new Faker();

    @Autowired
    private RideRepository rideRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private JdbcClient jdbcClient;

    @Test
    void shouldSoftDeleteRide() {
        var ride = rideRepository.save(createTestRideWithDriverInAcceptedStatus());

        rideRepository.deleteById(ride.getId());
        entityManager.flush();
        entityManager.clear();

        assertThat(rideRepository.findById(ride.getId())).isEmpty();

        var deletedRide = jdbcClient.sql("SELECT * FROM ride WHERE id = :id")
                .param("id", ride.getId())
                .query(Ride.class)
                .single();
        assertThat(deletedRide)
                .isNotNull()
                .satisfies(d -> {
                    assertThat(d.getId()).isEqualTo(ride.getId());
                    assertThat(d.getDeletedAt()).isNotNull();
                });
    }

    @Test
    void getAllShouldNotReturnSoftDeletedRides() {
        var ride1 = rideRepository.save(createTestRideWithDriverInAcceptedStatus());
        var ride2 = rideRepository.save(createTestRideWithDriverInAcceptedStatus());
        var ride3 = rideRepository.save(createTestRideWithDriverInAcceptedStatus());

        var ids = Stream.of(ride1, ride2, ride3).map(Ride::getId).toList();

        rideRepository.deleteById(ride1.getId());

        var result = rideRepository.findAll();

        assertThat(result)
                .extracting(Ride::getId)
                .filteredOn(ids::contains)
                .containsExactlyInAnyOrder(ride2.getId(), ride3.getId());
    }

    @Test
    void existsByIdShouldReturnFalseForSoftDeletedRide() {
        var ride = rideRepository.save(createTestRideWithDriverInAcceptedStatus());

        rideRepository.deleteById(ride.getId());

        assertThat(rideRepository.existsById(ride.getId()))
                .isFalse();
    }

    @Test
    void deleteShouldBeIdempotent() {
        var ride = rideRepository.save(createTestRideWithDriverInAcceptedStatus());

        rideRepository.deleteById(ride.getId());
        rideRepository.deleteById(ride.getId());

        assertThat(rideRepository.existsById(ride.getId()))
                .isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = RideStatus.class, names = {"CREATED", "CANCELED"}, mode = EnumSource.Mode.EXCLUDE)
    void throwExceptionWhenSaveWithActiveStatusAndNullDriver(RideStatus rideStatus) {
        var ride = createTestRideWithDriverInAcceptedStatus();
        ride.setDriverId(null);
        ride.setStatus(rideStatus);

        assertThatThrownBy(() -> rideRepository.save(ride))
                .isInstanceOfAny(ConstraintViolationException.class, DataIntegrityViolationException.class);
    }

    @Test
    void throwExceptionWhenDriverWithActiveRideAssignToAntherRide() {
        var ride = createTestRideWithDriverInAcceptedStatus();
        rideRepository.save(ride);
        var anotherRide = createTestRideWithDriverInAcceptedStatus();
        anotherRide.setDriverId(ride.getDriverId());

        assertThatThrownBy(() -> rideRepository.save(anotherRide))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void throwExceptionWhenSaveNegativePrice() {
        var ride = createTestRideWithDriverInAcceptedStatus();
        ride.setPrice(BigDecimal.TEN.negate());

        assertThatThrownBy(() -> rideRepository.save(ride))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private Ride createTestRideWithDriverInAcceptedStatus() {
        var random = new Random();
        var ride = new Ride();
        ride.setPassengerId(random.nextLong(1, Long.MAX_VALUE));
        ride.setDriverId(random.nextLong(1, Long.MAX_VALUE));
        ride.setStartAddress(faker.address().fullAddress());
        ride.setDestinationAddress(faker.address().fullAddress());
        ride.setStatus(RideStatus.ACCEPTED);
        ride.setCreatedAt(LocalDateTime.now());
        ride.setPrice(randomInRange());
        return ride;
    }

    private BigDecimal randomInRange() {
        BigDecimal range = BigDecimal.TEN.subtract(BigDecimal.ONE);
        BigDecimal randomFactor = BigDecimal.valueOf(Math.random());
        return BigDecimal.ONE.add(randomFactor.multiply(range)).setScale(2, RoundingMode.UP);
    }
}