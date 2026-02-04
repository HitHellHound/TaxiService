package com.efcon.driver.service;

import com.efcon.driver.AbstractIntegrationTest;
import com.efcon.driver.dto.CarRequest;
import com.efcon.driver.dto.CarResponse;
import com.efcon.driver.exception.EntityNotFoundException;
import net.datafaker.Faker;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CarServiceIntegrationTest extends AbstractIntegrationTest {
    @Autowired
    private CarService carService;

    private final Faker faker = new Faker();
    private final long nonExistentId = Long.MAX_VALUE;

    @Test
    void createShouldReturnCarWithIdAndSameDataAndPostEvent() {
        var number = "7777TT-7";
        var color = "black";
        var brand = "Lada";
        var carRequest = new CarRequest(number, color, brand);

        var result = carService.create(carRequest);

        assertThat(result)
                .extracting(CarResponse::id, CarResponse::number, CarResponse::color, CarResponse::brand)
                .doesNotContainNull()
                .containsExactly(result.id(), carRequest.number(), carRequest.color(), carRequest.brand());
    }

    @Test
    void shouldGetExistingCar() {
        var car = carService.create(createTestCar());

        var result = carService.get(car.id());

        assertThat(result).isEqualTo(car);
    }

    @Test
    void throwEntityNotFoundExceptionForNonExistingCar() {
        assertThatThrownBy(() -> carService.get(nonExistentId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Car with id " + nonExistentId + " not found");
    }

    @Test
    void shouldGetAllExistingCars() {
        var car1 = carService.create(createTestCar());
        var car2 = carService.create(createTestCar());
        var car3 = carService.create(createTestCar());

        var ids = Stream.of(car1, car2, car3).map(CarResponse::id).collect(Collectors.toSet());

        var result = carService.getAll();

        assertThat(result)
                .isNotEmpty()
                .filteredOn(car -> ids.contains(car.id()))
                .extracting(CarResponse::id)
                .containsExactlyInAnyOrderElementsOf(ids);
    }

    @Test
    void updateShouldChangeCar() {
        var car = carService.create(createTestCar());
        var newCarData = createTestCar();

        var updatedCar = carService.update(car.id(), newCarData);

        assertThat(updatedCar)
                .extracting(CarResponse::id, CarResponse::number, CarResponse::color, CarResponse::brand)
                .containsExactly(car.id(), newCarData.number(), newCarData.color(), newCarData.brand());

        assertThat(carService.get(car.id()))
                .isEqualTo(updatedCar);
    }

    @Test
    void updateThrowEntityNotFoundExceptionForNonExistingCar() {
        var newCarData = createTestCar();
        assertThatThrownBy(() -> carService.update(nonExistentId, newCarData))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Car with id " + nonExistentId + " not found");
    }

    @Test
    void deleteShouldMakeCarInaccessible() {
        var car = carService.create(createTestCar());

        carService.delete(car.id());

        assertThatThrownBy(() -> carService.get(car.id()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Car with id " + car.id() + " not found");

        assertThat(carService.getAll())
                .filteredOn(c -> Objects.equals(c.id(), car.id()))
                .isEmpty();
    }

    private CarRequest createTestCar() {
        var uuid = UUID.randomUUID();
        var number = faker.regexify("\\d{4}[A-Z]{2}-[1-8]");
        var color = "#" + String.format("%06x", new Random().nextInt(0x1000000));
        var brand = "brand #" + uuid;
        return new CarRequest(number, color, brand);
    }
}
