package com.efcon.ride.controller;

import com.efcon.ride.dto.DriverAssignment;
import com.efcon.ride.dto.RideRequest;
import com.efcon.ride.dto.RideResponse;
import com.efcon.ride.exception.EntityNotFoundException;
import com.efcon.ride.service.RideService;
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
class RideControllerTest {
    @Mock
    private RideService rideService;

    @InjectMocks
    private RideController rideController;

    @Test
    void getAllShouldUseRideService() {
        var ride = Mockito.mock(RideResponse.class);
        var rideList = List.of(ride);
        Mockito.when(rideService.getAll()).thenReturn(rideList);

        var result = rideController.getAll();

        assertThat(result).isEqualTo(rideList);
        Mockito.verify(rideService).getAll();
    }

    @Test
    void getByIdShouldUseRideService() {
        var rideId = 1L;
        var ride = Mockito.mock(RideResponse.class);
        Mockito.when(rideService.get(rideId)).thenReturn(ride);

        var result = rideController.getById(rideId);

        assertThat(result).isEqualTo(ride);
        Mockito.verify(rideService).get(rideId);
    }

    @Test
    void getByIdShouldNotProcessEntityNotFoundException() {
        var rideId = 1L;
        Mockito.when(rideService.get(rideId)).thenThrow(new EntityNotFoundException(""));

        assertThatThrownBy(() -> rideController.getById(rideId))
                .isInstanceOf(EntityNotFoundException.class);
        Mockito.verify(rideService).get(rideId);
    }

    @Test
    void createShouldUseRideServiceAndReturnCreatedStatusResponse() {
        var rideRequest = Mockito.mock(RideRequest.class);
        var ride = Mockito.mock(RideResponse.class);
        Mockito.when(rideService.create(rideRequest)).thenReturn(ride);

        var result = rideController.create(rideRequest);


        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isEqualTo(ride);
        Mockito.verify(rideService).create(rideRequest);
    }

    @Test
    void updateShouldUseRideService() {
        var rideId = 1L;
        var rideRequest = Mockito.mock(RideRequest.class);
        var ride = Mockito.mock(RideResponse.class);
        Mockito.when(rideService.update(rideId, rideRequest)).thenReturn(ride);

        var result = rideController.update(rideId, rideRequest);

        assertThat(result).isEqualTo(ride);
        Mockito.verify(rideService).update(rideId, rideRequest);
    }

    @Test
    void updateShouldNotProcessEntityNotFoundException() {
        var rideId = 1L;
        var rideRequest = Mockito.mock(RideRequest.class);
        Mockito.when(rideService.update(rideId, rideRequest)).thenThrow(new EntityNotFoundException(""));

        assertThatThrownBy(() -> rideController.update(rideId, rideRequest))
                .isInstanceOf(EntityNotFoundException.class);
        Mockito.verify(rideService).update(rideId, rideRequest);
    }

    @Test
    void deleteShouldUseRideServiceAndReturnNoContentStatusResponse() {
        var rideId = 1L;

        var result = rideController.delete(rideId);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(result.hasBody()).isFalse();
        Mockito.verify(rideService).delete(rideId);
    }

    @Test
    void acceptRideShouldUseRideService() {
        var rideId = 1L;
        var driverId = 2L;
        var driver = Mockito.mock(DriverAssignment.class);
        Mockito.when(driver.driverId()).thenReturn(driverId);

        var ride = Mockito.mock(RideResponse.class);
        Mockito.when(rideService.accept(rideId, driverId)).thenReturn(ride);

        var result = rideController.acceptRide(rideId, driver);

        assertThat(result).isEqualTo(ride);
        Mockito.verify(rideService).accept(rideId, driverId);
    }

    @Test
    void driveToPassengerRideShouldUseRideService() {
        var rideId = 1L;
        var ride = Mockito.mock(RideResponse.class);
        Mockito.when(rideService.driveToPassenger(rideId)).thenReturn(ride);

        var result = rideController.driveToPassenger(rideId);

        assertThat(result).isEqualTo(ride);
        Mockito.verify(rideService).driveToPassenger(rideId);
    }

    @Test
    void driveToDestinationRideShouldUseRideService() {
        var rideId = 1L;
        var ride = Mockito.mock(RideResponse.class);
        Mockito.when(rideService.driveToDestination(rideId)).thenReturn(ride);

        var result = rideController.driveToDestination(rideId);

        assertThat(result).isEqualTo(ride);
        Mockito.verify(rideService).driveToDestination(rideId);
    }

    @Test
    void completeRideRideShouldUseRideService() {
        var rideId = 1L;
        var ride = Mockito.mock(RideResponse.class);
        Mockito.when(rideService.complete(rideId)).thenReturn(ride);

        var result = rideController.completeRide(rideId);

        assertThat(result).isEqualTo(ride);
        Mockito.verify(rideService).complete(rideId);
    }

    @Test
    void cancelRideShouldUseRideService() {
        var rideId = 1L;
        var ride = Mockito.mock(RideResponse.class);
        Mockito.when(rideService.cancel(rideId)).thenReturn(ride);

        var result = rideController.cancelRide(rideId);

        assertThat(result).isEqualTo(ride);
        Mockito.verify(rideService).cancel(rideId);
    }
}