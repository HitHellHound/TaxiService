package com.efcon.passenger.controller;

import com.efcon.passenger.dto.PassengerRequest;
import com.efcon.passenger.dto.PassengerResponse;
import com.efcon.passenger.exception.EntityNotFoundException;
import com.efcon.passenger.model.Passenger;
import com.efcon.passenger.service.PassengerServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PassengerControllerTest {
    @Mock
    private PassengerServiceImpl passengerService;
    @InjectMocks
    private PassengerController passengerController;

    @Test
    void getAllShouldUsePassengerService() {
        var passenger = Mockito.mock(PassengerResponse.class);
        var passengerList = List.of(passenger);
        Mockito.when(passengerService.getAll()).thenReturn(passengerList);

        var result = passengerController.getAll();

        assertThat(result).isEqualTo(passengerList);
        Mockito.verify(passengerService).getAll();
    }

    @Test
    void getByIdShouldUsePassengerService() {
        var passengerId = 1L;
        var passenger = Mockito.mock(PassengerResponse.class);
        Mockito.when(passengerService.get(passengerId)).thenReturn(passenger);

        var result = passengerController.getById(passengerId);

        assertThat(result).isEqualTo(passenger);
        Mockito.verify(passengerService).get(passengerId);
    }

    @Test
    void getByIdShouldNotProcessEntityNotFoundException() {
        var passengerId = 1L;
        Mockito.when(passengerService.get(passengerId)).thenThrow(new EntityNotFoundException());

        assertThatThrownBy(() -> passengerController.getById(passengerId))
                .isInstanceOf(EntityNotFoundException.class);
        Mockito.verify(passengerService).get(passengerId);
    }

    @Test
    void createShouldUsePassengerServiceAndReturnCreatedStatusResponse() {
        var passengerRequest = Mockito.mock(PassengerRequest.class);
        var passenger = Mockito.mock(PassengerResponse.class);
        Mockito.when(passengerService.create(passengerRequest)).thenReturn(passenger);

        var result = passengerController.create(passengerRequest);


        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isEqualTo(passenger);
        Mockito.verify(passengerService).create(passengerRequest);
    }

    @Test
    void updateShouldUsePassengerService() {
        var passengerId = 1L;
        var passengerRequest = Mockito.mock(PassengerRequest.class);
        var passenger = Mockito.mock(PassengerResponse.class);
        Mockito.when(passengerService.update(passengerId, passengerRequest)).thenReturn(passenger);

        var result = passengerController.update(passengerId, passengerRequest);

        assertThat(result).isEqualTo(passenger);
        Mockito.verify(passengerService).update(passengerId, passengerRequest);
    }

    @Test
    void updateShouldNotProcessEntityNotFoundException() {
        var passengerId = 1L;
        var passengerRequest = Mockito.mock(PassengerRequest.class);
        Mockito.when(passengerService.update(passengerId, passengerRequest)).thenThrow(new EntityNotFoundException());

        assertThatThrownBy(() -> passengerController.update(passengerId, passengerRequest))
                .isInstanceOf(EntityNotFoundException.class);
        Mockito.verify(passengerService).update(passengerId, passengerRequest);
    }

    @Test
    void deleteShouldUsePassengerServiceAndReturnNoContentStatusResponse() {
        var passengerId = 1L;

        var result = passengerController.delete(passengerId);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        Mockito.verify(passengerService).delete(passengerId);
    }
}