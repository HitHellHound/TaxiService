package com.efcon.driver.repository;

import com.efcon.driver.AbstractIntegrationTest;
import com.efcon.driver.model.Car;
import com.efcon.driver.model.Driver;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import net.datafaker.Faker;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DriverRepositoryTest extends AbstractIntegrationTest {
    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private CarRepository carRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private JdbcClient jdbcClient;

    private final Faker faker = new Faker();

    @Test
    void shouldSoftDeleteDriver() {
        var driver = driverRepository.save(createTestDriver());

        driverRepository.deleteById(driver.getId());
        entityManager.flush();
        entityManager.clear();

        assertThat(driverRepository.findById(driver.getId())).isEmpty();

        var deletedDriver = jdbcClient.sql("SELECT * FROM driver WHERE id = :id")
                .param("id", driver.getId())
                .query(Driver.class)
                .single();
        assertThat(deletedDriver)
                .isNotNull()
                .satisfies(d -> {
                    assertThat(d.getId()).isEqualTo(driver.getId());
                    assertThat(d.getDeletedAt()).isNotNull();
                });
    }

    @Test
    void getAllShouldNotReturnSoftDeletedDrivers() {
        var driver1 = driverRepository.save(createTestDriver());
        var driver2 = driverRepository.save(createTestDriver());
        var driver3 = driverRepository.save(createTestDriver());

        var ids = Stream.of(driver1, driver2, driver3).map(Driver::getId).toList();

        driverRepository.deleteById(driver1.getId());

        var result = driverRepository.findAll();

        assertThat(result)
                .extracting(Driver::getId)
                .filteredOn(ids::contains)
                .containsExactlyInAnyOrder(driver2.getId(), driver3.getId());
    }

    @Test
    void existsByIdShouldReturnFalseForSoftDeletedDriver() {
        var driver = driverRepository.save(createTestDriver());

        driverRepository.deleteById(driver.getId());

        assertThat(driverRepository.existsById(driver.getId()))
                .isFalse();
    }

    @Test
    void deleteShouldBeIdempotent() {
        var driver = driverRepository.save(createTestDriver());

        driverRepository.deleteById(driver.getId());
        driverRepository.deleteById(driver.getId());

        assertThat(driverRepository.existsById(driver.getId()))
                .isFalse();
    }

    @Test
    void shouldThrowExceptionIfSaveDriverWithExistingEmail() {
        var driver1 = driverRepository.save(createTestDriver());
        var driver2 = createTestDriver();
        driver2.setEmail(driver1.getEmail());

        assertThatThrownBy(() -> driverRepository.save(driver2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldThrowExceptionIfSaveDriverWithExistingPhone() {
        var driver1 = driverRepository.save(createTestDriver());
        var driver2 = createTestDriver();
        driver2.setPhone(driver1.getPhone());

        assertThatThrownBy(() -> driverRepository.save(driver2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldThrowExceptionWhenSaveDriverWithAlreadyAssignedCar() {
        var driver1 = createTestDriver();
        var car = carRepository.save(new Car("color", "brand", "1111AT-7"));
        driver1.setCar(car);
        driverRepository.save(driver1);
        entityManager.flush();
        entityManager.clear();

        var driver2 = driverRepository.save(createTestDriver());
        driver2.setCar(car);

        assertThatThrownBy(() -> {
            driverRepository.saveAndFlush(driver2);
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    private Driver createTestDriver() {
        var uuid = UUID.randomUUID();
        var name = "John Doe #" + uuid;
        var email = "john.doe." + uuid + "@test.org";
        var phoneNumber = faker.regexify("\\+375(25|29|33|44)\\d{7}");
        return new Driver(name, email, phoneNumber);
    }
}