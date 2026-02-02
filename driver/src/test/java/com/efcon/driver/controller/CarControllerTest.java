package com.efcon.driver.controller;

import com.efcon.driver.dto.CarRequest;
import com.efcon.driver.dto.CarResponse;
import com.efcon.driver.exception.EntityNotFoundException;
import com.efcon.driver.service.CarService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class CarControllerTest {
    @Mock
    private CarService carService;

    @InjectMocks
    private CarController carController;

    @Test
    void getAllShouldUseCarService() {
        var car = Mockito.mock(CarResponse.class);
        var carList = List.of(car);
        Mockito.when(carService.getAll()).thenReturn(carList);

        var result = carController.getAll();

        assertThat(result).isEqualTo(carList);
        Mockito.verify(carService).getAll();
    }

    @Test
    void getByIdShouldUseCarService() {
        var carId = 1L;
        var car = Mockito.mock(CarResponse.class);
        Mockito.when(carService.get(carId)).thenReturn(car);

        var result = carController.getById(carId);

        assertThat(result).isEqualTo(car);
        Mockito.verify(carService).get(carId);
    }

    @Test
    void getByIdShouldNotProcessEntityNotFoundException() {
        var carId = 1L;
        Mockito.when(carService.get(carId)).thenThrow(new EntityNotFoundException(""));

        assertThatThrownBy(() -> carController.getById(carId))
                .isInstanceOf(EntityNotFoundException.class);
        Mockito.verify(carService).get(carId);
    }

    @Test
    void createShouldUsecarServiceAndReturnCreatedStatusResponse() {
        var carRequest = Mockito.mock(CarRequest.class);
        var car = Mockito.mock(CarResponse.class);
        Mockito.when(carService.create(carRequest)).thenReturn(car);

        var result = carController.create(carRequest);


        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isEqualTo(car);
        Mockito.verify(carService).create(carRequest);
    }

    @Test
    void updateShouldUseCarService() {
        var carId = 1L;
        var carRequest = Mockito.mock(CarRequest.class);
        var car = Mockito.mock(CarResponse.class);
        Mockito.when(carService.update(carId, carRequest)).thenReturn(car);

        var result = carController.update(carId, carRequest);

        assertThat(result).isEqualTo(car);
        Mockito.verify(carService).update(carId, carRequest);
    }

    @Test
    void updateShouldNotProcessEntityNotFoundException() {
        var carId = 1L;
        var carRequest = Mockito.mock(CarRequest.class);
        Mockito.when(carService.update(carId, carRequest)).thenThrow(new EntityNotFoundException(""));

        assertThatThrownBy(() -> carController.update(carId, carRequest))
                .isInstanceOf(EntityNotFoundException.class);
        Mockito.verify(carService).update(carId, carRequest);
    }

    @Test
    void deleteShouldUseCarServiceAndReturnNoContentStatusResponse() {
        var carId = 1L;

        var result = carController.delete(carId);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(result.hasBody()).isFalse();
        Mockito.verify(carService).delete(carId);
    }
}