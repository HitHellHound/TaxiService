package com.efcon.driver.repository;

import com.efcon.driver.AbstractIntegrationTest;
import com.efcon.driver.model.Car;
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

class CarRepositoryTest extends AbstractIntegrationTest {
    @Autowired
    private CarRepository carRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private JdbcClient jdbcClient;

    private final Faker faker = new Faker();

    @Test
    void shouldSoftDeleteCar() {
        var car = carRepository.save(createTestCar());

        carRepository.deleteById(car.getId());
        entityManager.flush();
        entityManager.clear();

        assertThat(carRepository.findById(car.getId())).isEmpty();

        var deletedCar = jdbcClient.sql("SELECT * FROM car WHERE id = :id")
                .param("id", car.getId())
                .query(Car.class)
                .single();
        assertThat(deletedCar)
                .isNotNull()
                .satisfies(d -> {
                    assertThat(d.getId()).isEqualTo(car.getId());
                    assertThat(d.getDeletedAt()).isNotNull();
                });
    }

    @Test
    void getAllShouldNotReturnSoftDeletedCars() {
        var car1 = carRepository.save(createTestCar());
        var car2 = carRepository.save(createTestCar());
        var car3 = carRepository.save(createTestCar());

        var ids = Stream.of(car1, car2, car3).map(Car::getId).toList();

        carRepository.deleteById(car1.getId());

        var result = carRepository.findAll();

        assertThat(result)
                .extracting(Car::getId)
                .filteredOn(ids::contains)
                .containsExactlyInAnyOrder(car2.getId(), car3.getId());
    }

    @Test
    void existsByIdShouldReturnFalseForSoftDeletedCar() {
        var car = carRepository.save(createTestCar());

        carRepository.deleteById(car.getId());

        assertThat(carRepository.existsById(car.getId()))
                .isFalse();
    }

    @Test
    void deleteShouldBeIdempotent() {
        var car = carRepository.save(createTestCar());

        carRepository.deleteById(car.getId());
        carRepository.deleteById(car.getId());

        assertThat(carRepository.existsById(car.getId()))
                .isFalse();
    }

    @Test
    void shouldThrowExceptionIfSaveCarWithExistingNumber() {
        var car1 = carRepository.save(createTestCar());
        var car2 = createTestCar();
        car2.setNumber(car1.getNumber());

        assertThatThrownBy(() -> carRepository.save(car2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private Car createTestCar() {
        var uuid = UUID.randomUUID();
        var color = "Color #" + uuid;
        var brand = "Brand #" + uuid;
        var number = faker.regexify("\\d{4}[A-Z]{2}-[1-8]");
        return new Car(color, brand, number);
    }
}