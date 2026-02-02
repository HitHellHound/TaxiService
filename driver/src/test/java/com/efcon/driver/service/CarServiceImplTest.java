package com.efcon.driver.service;

import com.efcon.driver.dto.CarRequest;
import com.efcon.driver.dto.CarResponse;
import com.efcon.driver.exception.EntityNotFoundException;
import com.efcon.driver.mapper.CarMapper;
import com.efcon.driver.model.Car;
import com.efcon.driver.repository.CarRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class CarServiceImplTest {
    @Mock
    private CarRepository repository;

    @Mock
    private CarMapper mapper;

    @InjectMocks
    private CarServiceImpl carService;

    @Test
    void getAllShouldUseRepositoryFindAll() {
        var car = Mockito.mock(Car.class);
        var carList = List.of(car);
        Mockito.when(repository.findAll()).thenReturn(carList);

        var carResponse =  Mockito.mock(CarResponse.class);
        var carResponseList = List.of(carResponse);
        Mockito.when(mapper.toResponseList(carList)).thenReturn(carResponseList);

        var result = carService.getAll();

        assertThat(result)
                .isNotEmpty()
                .isEqualTo(carResponseList);
        Mockito.verify(repository).findAll();
        Mockito.verify(mapper).toResponseList(carList);
    }

    @Test
    void getShouldUseRepositoryFindById() {
        var carId = 1L;
        var car = Mockito.mock(Car.class);
        Mockito.when(repository.findById(carId)).thenReturn(Optional.of(car));

        var carResponse =  Mockito.mock(CarResponse.class);
        Mockito.when(mapper.toResponse(car)).thenReturn(carResponse);

        var result = carService.get(carId);

        assertThat(result).isEqualTo(carResponse);
        Mockito.verify(repository).findById(carId);
        Mockito.verify(mapper).toResponse(car);
    }

    @Test
    void getThrowEntityNotFoundException() {
        var carId = 1L;
        Mockito.when(repository.findById(carId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> carService.get(carId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Car with id " + carId + " not found");
        Mockito.verify(repository).findById(carId);
    }

    @Test
    void createUseRepositorySave() {
        var carRequest = Mockito.mock(CarRequest.class);
        var newCar = Mockito.mock(Car.class);
        Mockito.when(mapper.fromRequest(carRequest)).thenReturn(newCar);
        Mockito.when(repository.save(newCar)).thenReturn(newCar);

        var carResponse =  Mockito.mock(CarResponse.class);
        Mockito.when(mapper.toResponse(newCar)).thenReturn(carResponse);

        var result = carService.create(carRequest);

        assertThat(result).isEqualTo(carResponse);
        Mockito.verify(mapper).fromRequest(carRequest);
        Mockito.verify(repository).save(newCar);
        Mockito.verify(mapper).toResponse(newCar);
    }

    @Test
    void updateShouldUseRepositorySave() {
        var carId = 1L;
        var carRequest = Mockito.mock(CarRequest.class);
        var car = Mockito.mock(Car.class);
        Mockito.when(repository.findById(carId)).thenReturn(Optional.of(car));
        Mockito.when(repository.save(car)).thenReturn(car);

        var carResponse =  Mockito.mock(CarResponse.class);
        Mockito.when(mapper.toResponse(car)).thenReturn(carResponse);

        var result = carService.update(carId, carRequest);

        assertThat(result).isEqualTo(carResponse);
        Mockito.verify(mapper).updateEntityFromRequest(carRequest, car);
        Mockito.verify(repository).save(car);
        Mockito.verify(mapper).toResponse(car);
    }

    @Test
    void updateThrowEntityNotFoundException() {
        var carId = 1L;
        var carRequest = Mockito.mock(CarRequest.class);
        Mockito.when(repository.findById(carId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> carService.update(carId, carRequest))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Car with id " + carId + " not found");
        Mockito.verify(repository).findById(carId);
    }

    @Test
    void deleteShouldUseRepositoryDeleteById() {
        var carId = 1L;

        carService.delete(carId);

        Mockito.verify(repository).deleteById(carId);
    }
}