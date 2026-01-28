package com.efcon.passenger.service;

import com.efcon.passenger.dto.PassengerRequest;
import com.efcon.passenger.dto.PassengerResponse;
import com.efcon.passenger.exception.EntityNotFoundException;
import com.efcon.passenger.mapper.PassengerMapper;
import com.efcon.passenger.model.Passenger;
import com.efcon.passenger.repository.PassengerRepository;
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
class PassengerServiceImplTest {
    @Mock
    private PassengerRepository repository;
    @Mock
    private PassengerMapper mapper;
    @InjectMocks
    private PassengerServiceImpl passengerService;

    @Test
    void getAllShouldReturnPassengerResponseList() {
        var passenger = Mockito.mock(Passenger.class);
        var passengerList = List.of(passenger);
        Mockito.when(repository.findAll()).thenReturn(passengerList);

        var passengerResponse = Mockito.mock(PassengerResponse.class);
        var passengerResponseList = List.of(passengerResponse);
        Mockito.when(mapper.toResponseList(passengerList)).thenReturn(passengerResponseList);

        var result = passengerService.getAll();

        assertThat(result).isEqualTo(passengerResponseList);
        Mockito.verify(repository).findAll();
        Mockito.verify(mapper).toResponseList(passengerList);
    }

    @Test
    void getShouldUseRepositoryFindById() {
        var passengerId = 1L;
        var passenger = Mockito.mock(Passenger.class);
        Mockito.when(repository.findById(passengerId)).thenReturn(Optional.of(passenger));
        var passengerResponse = Mockito.mock(PassengerResponse.class);
        Mockito.when(mapper.toResponse(passenger)).thenReturn(passengerResponse);

        var result = passengerService.get(passengerId);

        assertThat(result).isEqualTo(passengerResponse);
        Mockito.verify(repository).findById(passengerId);
        Mockito.verify(mapper).toResponse(passenger);
    }

    @Test
    void getThrowEntityNotFoundException() {
        var passengerId = 1L;
        Mockito.when(repository.findById(passengerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> passengerService.get(passengerId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Passenger with id " + passengerId + " not found");
        Mockito.verify(repository).findById(passengerId);
    }

    @Test
    void createShouldUseRepositorySave() {
        var passengerRequest = Mockito.mock(PassengerRequest.class);
        var newPassenger = Mockito.mock(Passenger.class);
        Mockito.when(mapper.fromRequest(passengerRequest)).thenReturn(newPassenger);
        Mockito.when(repository.save(newPassenger)).thenReturn(newPassenger);
        var passengerResponse = Mockito.mock(PassengerResponse.class);
        Mockito.when(mapper.toResponse(newPassenger)).thenReturn(passengerResponse);

        var result = passengerService.create(passengerRequest);

        assertThat(result).isEqualTo(passengerResponse);
        Mockito.verify(repository).save(newPassenger);
        Mockito.verify(mapper).toResponse(newPassenger);
    }

    @Test
    void updateShouldUseRepositorySaveForPassengerRequest() {
        var passengerId = 1L;
        var passengerRequest = Mockito.mock(PassengerRequest.class);
        var passenger = Mockito.mock(Passenger.class);
        Mockito.when(repository.findById(passengerId)).thenReturn(Optional.of(passenger));
        Mockito.when(repository.save(passenger)).thenReturn(passenger);
        var passengerResponse = Mockito.mock(PassengerResponse.class);
        Mockito.when(mapper.toResponse(passenger)).thenReturn(passengerResponse);

        var result = passengerService.update(passengerId, passengerRequest);

        assertThat(result).isEqualTo(passengerResponse);
        Mockito.verify(repository).findById(passengerId);
        Mockito.verify(mapper).updateEntityFromRequest(passengerRequest, passenger);
        Mockito.verify(repository).save(passenger);
        Mockito.verify(mapper).toResponse(passenger);
    }

    @Test
    void updateThrowEntityNotFoundException() {
        var passengerId = 1L;
        var passengerRequest = Mockito.mock(PassengerRequest.class);
        Mockito.when(repository.findById(passengerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> passengerService.update(passengerId, passengerRequest))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Passenger with id " + passengerId + " not found");
        Mockito.verify(repository).findById(passengerId);
    }

    @Test
    void deleteShouldUseRepositoryDeleteById() {
        var passengerId = 1L;

        passengerService.delete(passengerId);

        Mockito.verify(repository).deleteById(passengerId);
    }
}