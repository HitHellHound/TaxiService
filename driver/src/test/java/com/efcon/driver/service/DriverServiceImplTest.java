package com.efcon.driver.service;

import com.efcon.driver.dto.DriverInfo;
import com.efcon.driver.dto.DriverRequest;
import com.efcon.driver.dto.DriverResponse;
import com.efcon.driver.event.*;
import com.efcon.driver.exception.EntityNotFoundException;
import com.efcon.driver.mapper.DriverInfoMapper;
import com.efcon.driver.mapper.DriverMapper;
import com.efcon.driver.model.Car;
import com.efcon.driver.model.Driver;
import com.efcon.driver.repository.CarRepository;
import com.efcon.driver.repository.DriverRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class DriverServiceImplTest {
    @Mock
    private DriverRepository repository;
    @Mock
    private CarRepository carRepository;
    @Mock
    private DriverMapper mapper;
    @Mock
    private DriverInfoMapper driverInfoMapper;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    
    @InjectMocks
    private DriverServiceImpl driverService;

    @Test
    void getAllShouldUseRepositoryFindAll() {
        var driver = Mockito.mock(Driver.class);
        var driverList = List.of(driver);
        Mockito.when(repository.findAll()).thenReturn(driverList);

        var driverResponse =  Mockito.mock(DriverResponse.class);
        var driverResponseList = List.of(driverResponse);
        Mockito.when(mapper.toResponseList(driverList)).thenReturn(driverResponseList);

        var result = driverService.getAll();

        assertThat(result)
                .isNotEmpty()
                .isEqualTo(driverResponseList);
        Mockito.verify(repository).findAll();
        Mockito.verify(mapper).toResponseList(driverList);
    }

    @Test
    void getShouldUseRepositoryFindById() {
        var driverId = 1L;
        var driver = Mockito.mock(Driver.class);
        Mockito.when(repository.findById(driverId)).thenReturn(Optional.of(driver));

        var driverResponse =  Mockito.mock(DriverResponse.class);
        Mockito.when(mapper.toResponse(driver)).thenReturn(driverResponse);

        var result = driverService.get(driverId);

        assertThat(result).isEqualTo(driverResponse);
        Mockito.verify(repository).findById(driverId);
        Mockito.verify(mapper).toResponse(driver);
    }

    @Test
    void getThrowEntityNotFoundException() {
        var driverId = 1L;
        Mockito.when(repository.findById(driverId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> driverService.get(driverId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Driver with id " + driverId + " not found");
        Mockito.verify(repository).findById(driverId);
    }

    @Test
    void createUseRepositorySave() {
        var driverRequest = Mockito.mock(DriverRequest.class);
        var newdriver = Mockito.mock(Driver.class);
        Mockito.when(mapper.fromRequest(driverRequest)).thenReturn(newdriver);
        Mockito.when(repository.save(newdriver)).thenReturn(newdriver);

        var newDriverId = 1L;
        Mockito.when(newdriver.getId()).thenReturn(newDriverId);
        var driverInfo = Mockito.mock(DriverInfo.class);
        Mockito.when(driverInfoMapper.toDriverInfo(newdriver)).thenReturn(driverInfo);
        ArgumentCaptor<DriverCreatedEvent> eventCaptor = ArgumentCaptor.forClass(DriverCreatedEvent.class);

        var driverResponse =  Mockito.mock(DriverResponse.class);
        Mockito.when(mapper.toResponse(newdriver)).thenReturn(driverResponse);

        var result = driverService.create(driverRequest);

        assertThat(result).isEqualTo(driverResponse);
        Mockito.verify(mapper).fromRequest(driverRequest);
        Mockito.verify(repository).save(newdriver);
        Mockito.verify(mapper).toResponse(newdriver);

        Mockito.verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        DriverCreatedEvent driverCreatedEvent = eventCaptor.getValue();
        assertThat(driverCreatedEvent).satisfies(event -> {
            assertThat(event.driverId()).isEqualTo(newDriverId);
            assertThat(event.driverInfo()).isEqualTo(driverInfo);
        });
    }

    @Test
    void updateShouldUseRepositorySave() {
        //GIVEN
        var driverId = 1L;
        var driverRequest = Mockito.mock(DriverRequest.class);
        var driver = Mockito.mock(Driver.class);
        Mockito.when(repository.findById(driverId)).thenReturn(Optional.of(driver));
        Mockito.when(repository.save(driver)).thenReturn(driver);

        Mockito.when(driver.getId()).thenReturn(driverId);
        var driverInfo = Mockito.mock(DriverInfo.class);
        Mockito.when(driverInfoMapper.toDriverInfo(driver)).thenReturn(driverInfo);
        ArgumentCaptor<DriverChangedEvent> eventCaptor = ArgumentCaptor.forClass(DriverChangedEvent.class);

        var driverResponse =  Mockito.mock(DriverResponse.class);
        Mockito.when(mapper.toResponse(driver)).thenReturn(driverResponse);

        //WHEN
        var result = driverService.update(driverId, driverRequest);

        //THEN
        assertThat(result).isEqualTo(driverResponse);
        Mockito.verify(mapper).updateEntityFromRequest(driverRequest, driver);
        Mockito.verify(repository).save(driver);
        Mockito.verify(mapper).toResponse(driver);

        Mockito.verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue()).satisfies(event -> {
            assertThat(event.driverId()).isEqualTo(driverId);
            assertThat(event.driverInfo()).isEqualTo(driverInfo);
        });
    }

    @Test
    void updateThrowEntityNotFoundException() {
        var driverId = 1L;
        var driverRequest = Mockito.mock(DriverRequest.class);
        Mockito.when(repository.findById(driverId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> driverService.update(driverId, driverRequest))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Driver with id " + driverId + " not found");
        Mockito.verify(repository).findById(driverId);
    }

    @Test
    void deleteShouldUseRepositoryDeleteById() {
        var carId = 1L;
        ArgumentCaptor<DriverDeletedEvent> eventCaptor = ArgumentCaptor.forClass(DriverDeletedEvent.class);

        driverService.delete(carId);

        Mockito.verify(repository).deleteById(carId);
        Mockito.verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue())
                .extracting(DriverDeletedEvent::driverId)
                .isEqualTo(carId);
    }

    @Test
    void attachCarShouldSaveDriverWithCar() {
        var driverId = 1L;
        var carId = 2L;
        var driver = Mockito.mock(Driver.class);
        Mockito.when(repository.findById(driverId)).thenReturn(Optional.of(driver));
        var car = Mockito.mock(Car.class);
        Mockito.when(carRepository.findById(carId)).thenReturn(Optional.of(car));
        Mockito.when(repository.save(driver)).thenReturn(driver);

        ArgumentCaptor<DriverShiftStartedEvent> driverShiftStartedEvent = ArgumentCaptor.forClass(DriverShiftStartedEvent.class);

        var driverResponse = Mockito.mock(DriverResponse.class);
        Mockito.when(mapper.toResponse(driver)).thenReturn(driverResponse);

        var result = driverService.attachCar(driverId, carId);

        assertThat(result).isEqualTo(driverResponse);
        Mockito.verify(repository).findById(driverId);
        Mockito.verify(carRepository).findById(carId);
        Mockito.verify(driver).setCar(car);
        Mockito.verify(mapper).toResponse(driver);

        Mockito.verify(applicationEventPublisher).publishEvent(driverShiftStartedEvent.capture());
        assertThat(driverShiftStartedEvent.getValue())
                .extracting(DriverShiftStartedEvent::driverId, DriverShiftStartedEvent::carId)
                .containsExactly(driverId, carId);
    }

    @Test
    void attachCarThrowExceptionWhenCarNotExists() {
        var driverId = 1L;
        var carId = 2L;
        var driver = Mockito.mock(Driver.class);
        Mockito.when(repository.findById(driverId)).thenReturn(Optional.of(driver));
        Mockito.when(carRepository.findById(carId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> driverService.attachCar(driverId, carId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Car with id " + carId + " not found");

        Mockito.verify(repository).findById(driverId);
        Mockito.verify(carRepository).findById(carId);
    }

    @Test
    void attachCarThrowExceptionWhenDriverNotExists() {
        var driverId = 1L;
        var carId = 2L;
        Mockito.when(repository.findById(driverId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> driverService.attachCar(driverId, carId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Driver with id " + driverId + " not found");

        Mockito.verify(repository).findById(driverId);
    }

    @Test
    void detachCar() {
        var driverId = 1L;
        var driver = Mockito.mock(Driver.class);
        Mockito.when(repository.findById(driverId)).thenReturn(Optional.of(driver));
        Mockito.when(repository.save(driver)).thenReturn(driver);

        ArgumentCaptor<DriverShiftEndedEvent> driverShiftEndedEvent = ArgumentCaptor.forClass(DriverShiftEndedEvent.class);

        driverService.detachCar(driverId);

        Mockito.verify(repository).findById(driverId);
        Mockito.verify(driver).setCar(null);

        Mockito.verify(applicationEventPublisher).publishEvent(driverShiftEndedEvent.capture());
        assertThat(driverShiftEndedEvent.getValue())
                .extracting(DriverShiftEndedEvent::driverId)
                .isEqualTo(driverId);
    }

    @Test
    void detachCarThrowExceptionWhenDriverNotExists() {
        var driverId = 1L;
        Mockito.when(repository.findById(driverId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> driverService.detachCar(driverId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Driver with id " + driverId + " not found");

        Mockito.verify(repository).findById(driverId);
    }
}