package com.efcon.driver.controller;

import com.efcon.driver.dto.DriverRequest;
import com.efcon.driver.dto.DriverResponse;
import com.efcon.driver.exception.EntityNotFoundException;
import com.efcon.driver.service.DriverService;
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
class DriverControllerTest {
    @Mock
    private DriverService driverService;

    @InjectMocks
    private DriverController driverController;

    @Test
    void getAllShouldUseDriverService() {
        var driver = Mockito.mock(DriverResponse.class);
        var driverList = List.of(driver);
        Mockito.when(driverService.getAll()).thenReturn(driverList);

        var result = driverController.getAll();

        assertThat(result).isEqualTo(driverList);
        Mockito.verify(driverService).getAll();
    }

    @Test
    void getByIdShouldUseDriverService() {
        var driverId = 1L;
        var driver = Mockito.mock(DriverResponse.class);
        Mockito.when(driverService.get(driverId)).thenReturn(driver);

        var result = driverController.getById(driverId);

        assertThat(result).isEqualTo(driver);
        Mockito.verify(driverService).get(driverId);
    }

    @Test
    void getByIdShouldNotProcessEntityNotFoundException() {
        var driverId = 1L;
        Mockito.when(driverService.get(driverId)).thenThrow(new EntityNotFoundException(""));

        assertThatThrownBy(() -> driverController.getById(driverId))
                .isInstanceOf(EntityNotFoundException.class);
        Mockito.verify(driverService).get(driverId);
    }

    @Test
    void createShouldUseDriverServiceAndReturnCreatedStatusResponse() {
        var driverRequest = Mockito.mock(DriverRequest.class);
        var driver = Mockito.mock(DriverResponse.class);
        Mockito.when(driverService.create(driverRequest)).thenReturn(driver);

        var result = driverController.create(driverRequest);


        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isEqualTo(driver);
        Mockito.verify(driverService).create(driverRequest);
    }

    @Test
    void updateShouldUseDriverService() {
        var driverId = 1L;
        var driverRequest = Mockito.mock(DriverRequest.class);
        var driver = Mockito.mock(DriverResponse.class);
        Mockito.when(driverService.update(driverId, driverRequest)).thenReturn(driver);

        var result = driverController.update(driverId, driverRequest);

        assertThat(result).isEqualTo(driver);
        Mockito.verify(driverService).update(driverId, driverRequest);
    }

    @Test
    void updateShouldNotProcessEntityNotFoundException() {
        var driverId = 1L;
        var driverRequest = Mockito.mock(DriverRequest.class);
        Mockito.when(driverService.update(driverId, driverRequest)).thenThrow(new EntityNotFoundException(""));

        assertThatThrownBy(() -> driverController.update(driverId, driverRequest))
                .isInstanceOf(EntityNotFoundException.class);
        Mockito.verify(driverService).update(driverId, driverRequest);
    }

    @Test
    void deleteShouldUseDriverServiceAndReturnNoContentStatusResponse() {
        var driverId = 1L;

        var result = driverController.delete(driverId);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(result.hasBody()).isFalse();
        Mockito.verify(driverService).delete(driverId);
    }

    @Test
    void attachCarShouldUseDriverServiceAndReturnCreatedStatusResponse() {
        var driverId = 1L;
        var carId = 2L;
        var driverResponse = Mockito.mock(DriverResponse.class);
        Mockito.when(driverService.attachCar(driverId, carId)).thenReturn(driverResponse);

        var result = driverController.attachCar(driverId, carId);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isEqualTo(driverResponse);
        Mockito.verify(driverService).attachCar(driverId, carId);
    }

    @Test
    void attachCarShouldNotProcessEntityNotFoundException() {
        var driverId = 1L;
        var carId = 2L;
        Mockito.when(driverService.attachCar(driverId, carId)).thenThrow(new EntityNotFoundException(""));

        assertThatThrownBy(() -> driverController.attachCar(driverId, carId))
                .isInstanceOf(EntityNotFoundException.class);
        Mockito.verify(driverService).attachCar(driverId, carId);
    }

    @Test
    void detachCarShouldUseDriverServiceAndReturnNoContentStatusResponse() {
        var driverId = 1L;

        var result = driverController.detachCarFromDriver(driverId);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(result.hasBody()).isFalse();
        Mockito.verify(driverService).detachCar(driverId);
    }
}