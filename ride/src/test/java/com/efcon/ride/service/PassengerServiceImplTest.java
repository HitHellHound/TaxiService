package com.efcon.ride.service;

import com.efcon.ride.dto.PassengerRequest;
import com.efcon.ride.dto.PassengerResponse;
import com.efcon.ride.external.client.PassengerClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class PassengerServiceImplTest {
    @Mock
    private PassengerClient passengerClient;

    @InjectMocks
    private PassengerServiceImpl passengerService;

    @Test
    void getAllShouldUsePassengerClient() {
        var passengerResponse1 = Mockito.mock(PassengerResponse.class);
        var passengerResponse2 = Mockito.mock(PassengerResponse.class);
        var passengerResponse3 = Mockito.mock(PassengerResponse.class);

        var passengerResponseList = List.of(passengerResponse1, passengerResponse2, passengerResponse3);
        Mockito.when(passengerClient.getAll()).thenReturn(passengerResponseList);

        passengerService.getAll();

        Mockito.verify(passengerClient).getAll();
    }

    @Test
    void getShouldUsePassengerClient() {
        var passengerId = 1L;
        var passengerResponse = Mockito.mock(PassengerResponse.class);
        Mockito.when(passengerClient.get(passengerId)).thenReturn(passengerResponse);

        var result = passengerService.get(passengerId);

        assertThat(result).isEqualTo(passengerResponse);
        Mockito.verify(passengerClient).get(passengerId);
    }

    @Test
    void createShouldUsePassengerClient() {
        var passengerRequest = Mockito.mock(PassengerRequest.class);
        var passengerResponse = Mockito.mock(PassengerResponse.class);
        Mockito.when(passengerClient.create(passengerRequest)).thenReturn(passengerResponse);

        var result = passengerService.create(passengerRequest);

        assertThat(result).isEqualTo(passengerResponse);
        Mockito.verify(passengerClient).create(passengerRequest);
    }

    @Test
    void updateShouldUsePassengerClient() {
        var passengerId = 1L;
        var passengerRequest = Mockito.mock(PassengerRequest.class);
        var passengerResponse = Mockito.mock(PassengerResponse.class);
        Mockito.when(passengerClient.update(passengerId, passengerRequest)).thenReturn(passengerResponse);

        var result = passengerService.update(passengerId, passengerRequest);

        assertThat(result).isEqualTo(passengerResponse);
        Mockito.verify(passengerClient).update(passengerId, passengerRequest);
    }

    @Test
    void deleteShouldUsePassengerClient() {
        var passengerId = 1L;

        passengerService.delete(passengerId);

        Mockito.verify(passengerClient).delete(passengerId);
    }
}