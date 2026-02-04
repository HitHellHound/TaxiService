package com.efcon.ride.service;

import com.efcon.ride.dto.PassengerResponse;
import com.efcon.ride.dto.RideInfo;
import com.efcon.ride.dto.RideRequest;
import com.efcon.ride.dto.RideResponse;
import com.efcon.ride.event.RideAcceptedEvent;
import com.efcon.ride.event.RideCanceledEvent;
import com.efcon.ride.event.RideCompletedEvent;
import com.efcon.ride.event.RideCreatedEvent;
import com.efcon.ride.exception.EntityNotFoundException;
import com.efcon.ride.exception.IllegalRideStatusTransitionException;
import com.efcon.ride.mapper.RideInfoMapper;
import com.efcon.ride.mapper.RideMapper;
import com.efcon.ride.model.DriverInfo;
import com.efcon.ride.model.DriverStatus;
import com.efcon.ride.model.Ride;
import com.efcon.ride.model.RideStatus;
import com.efcon.ride.repository.RideRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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
import static org.mockito.Mockito.atLeastOnce;

@ExtendWith(MockitoExtension.class)
class RideServiceImplTest {
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private PassengerService passengerService;
    @Mock
    private DriverInfoService driverInfoService;
    @Mock
    private RideRepository repository;
    @Mock
    private RideMapper mapper;
    @Mock
    private RideInfoMapper rideInfoMapper;

    @InjectMocks
    private RideServiceImpl rideService;

    @Test
    void getAllShouldUseRepositoryFindAll() {
        var ride = Mockito.mock(Ride.class);
        var rideList = List.of(ride);
        Mockito.when(repository.findAll()).thenReturn(rideList);

        var rideResponse = Mockito.mock(RideResponse.class);
        var rideResponseList = List.of(rideResponse);
        Mockito.when(mapper.toResponseList(rideList)).thenReturn(rideResponseList);

        var result = rideService.getAll();

        assertThat(result)
                .isNotEmpty()
                .isEqualTo(rideResponseList);
        Mockito.verify(repository).findAll();
        Mockito.verify(mapper).toResponseList(rideList);
    }

    @Test
    void getShouldUseRepositoryFindById() {
        var rideId = 1L;
        var ride = Mockito.mock(Ride.class);
        Mockito.when(repository.findById(rideId)).thenReturn(Optional.of(ride));

        var rideResponse =  Mockito.mock(RideResponse.class);
        Mockito.when(mapper.toResponse(ride)).thenReturn(rideResponse);

        var result = rideService.get(rideId);

        assertThat(result).isEqualTo(rideResponse);
        Mockito.verify(repository).findById(rideId);
        Mockito.verify(mapper).toResponse(ride);
    }

    @Test
    void getThrowEntityNotFoundException() {
        var driverId = 1L;
        Mockito.when(repository.findById(driverId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rideService.get(driverId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Ride with id " + driverId + " not found");
        Mockito.verify(repository).findById(driverId);
    }

    @Test
    void createShouldUseRepositorySaveAndRequestPassengerServiceAndPostEvent() {
        //GIVEN
        var passengerId = 1L;
        var rideRequest = Mockito.mock(RideRequest.class);
        Mockito.when(rideRequest.passengerId()).thenReturn(passengerId);

        var passenger = Mockito.mock(PassengerResponse.class);
        Mockito.when(passengerService.get(passengerId)).thenReturn(passenger);

        var newRide = Mockito.mock(Ride.class);
        Mockito.when(mapper.fromRequest(rideRequest)).thenReturn(newRide);
        Mockito.when(repository.save(newRide)).thenReturn(newRide);

        var newRideId = 2L;
        Mockito.when(newRide.getId()).thenReturn(newRideId);
        var rideInfo = Mockito.mock(RideInfo.class);
        Mockito.when(rideInfoMapper.toRideInfo(newRide)).thenReturn(rideInfo);

        ArgumentCaptor<RideCreatedEvent> eventCaptor = ArgumentCaptor.forClass(RideCreatedEvent.class);

        var rideResponse =  Mockito.mock(RideResponse.class);
        Mockito.when(mapper.toResponse(newRide)).thenReturn(rideResponse);

        //WHEN
        var result = rideService.create(rideRequest);

        //THEN
        assertThat(result).isEqualTo(rideResponse);
        Mockito.verify(passengerService).get(passengerId);
        Mockito.verify(mapper).fromRequest(rideRequest);
        Mockito.verify(repository).save(newRide);
        Mockito.verify(mapper).toResponse(newRide);

        Mockito.verify(eventPublisher).publishEvent(eventCaptor.capture());
        RideCreatedEvent rideCreatedEvent = eventCaptor.getValue();
        assertThat(rideCreatedEvent).satisfies(event -> {
            assertThat(event.rideId()).isEqualTo(newRideId);
            assertThat(event.rideInfo()).isEqualTo(rideInfo);
        });
    }

    @Test
    void createShouldNotProcessPassengerServiceExceptions() {
        var passengerId = 1L;
        var rideRequest = Mockito.mock(RideRequest.class);
        Mockito.when(rideRequest.passengerId()).thenReturn(passengerId);

        Mockito.when(passengerService.get(passengerId)).thenThrow(new EntityNotFoundException(""));

        assertThatThrownBy(() -> rideService.create(rideRequest));
        Mockito.verify(passengerService).get(passengerId);
    }

    @Test
    void updateShouldUseRepositorySave() {
        var rideId = 1L;
        var rideRequest = Mockito.mock(RideRequest.class);
        var ride = Mockito.mock(Ride.class);
        Mockito.when(repository.findById(rideId)).thenReturn(Optional.of(ride));
        Mockito.when(repository.save(ride)).thenReturn(ride);

        var rideResponse = Mockito.mock(RideResponse.class);
        Mockito.when(mapper.toResponse(ride)).thenReturn(rideResponse);

        var result = rideService.update(rideId, rideRequest);

        assertThat(result).isEqualTo(rideResponse);
        Mockito.verify(repository).findById(rideId);
        Mockito.verify(mapper).updateEntityFromRequest(rideRequest, ride);
        Mockito.verify(repository).save(ride);
        Mockito.verify(mapper).toResponse(ride);
    }

    @Test
    void updateThrowEntityNotFoundException() {
        var rideId = 1L;
        var rideRequest = Mockito.mock(RideRequest.class);
        Mockito.when(repository.findById(rideId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rideService.update(rideId, rideRequest))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Ride with id " + rideId + " not found");
        Mockito.verify(repository).findById(rideId);
    }

    @Test
    void deleteShouldUseRepositoryDeleteById() {
        var rideId = 1L;

        rideService.delete(rideId);

        Mockito.verify(repository).deleteById(rideId);
    }

    @ParameterizedTest
    @EnumSource(value = RideStatus.class, names = {"CREATED"})
    void acceptShouldWorkForCorrectStatusTransitions(RideStatus rideStatus) {
        //GIVEN
        var rideId = 1L;
        var driverId = 2L;
        var carId = 3L;

        var driverInfo = Mockito.mock(DriverInfo.class);
        Mockito.when(driverInfoService.get(driverId)).thenReturn(driverInfo);
        Mockito.when(driverInfo.getCarId()).thenReturn(carId);
        Mockito.when(driverInfo.getStatus()).thenReturn(DriverStatus.FREE);

        var ride = Mockito.mock(Ride.class);
        Mockito.when(repository.findById(rideId)).thenReturn(Optional.of(ride));
        Mockito.when(ride.getStatus()).thenReturn(rideStatus);

        Mockito.when(repository.save(ride)).thenReturn(ride);

        ArgumentCaptor<RideAcceptedEvent> eventCaptor = ArgumentCaptor.forClass(RideAcceptedEvent.class);

        var rideResponse = Mockito.mock(RideResponse.class);
        Mockito.when(mapper.toResponse(ride)).thenReturn(rideResponse);

        //WHEN
        var result = rideService.accept(rideId, driverId);

        //THEN
        assertThat(result).isEqualTo(rideResponse);
        Mockito.verify(driverInfo).getCarId();
        Mockito.verify(driverInfo).getStatus();
        Mockito.verify(ride).getStatus();
        Mockito.verify(ride).setStatus(RideStatus.ACCEPTED);
        Mockito.verify(ride).setDriverId(driverId);
        Mockito.verify(driverInfoService).changeDriverStatus(driverId, DriverStatus.ON_TRIP);
        Mockito.verify(repository).save(ride);

        Mockito.verify(eventPublisher).publishEvent(eventCaptor.capture());
        RideAcceptedEvent rideAcceptedEvent = eventCaptor.getValue();
        assertThat(rideAcceptedEvent).satisfies(event -> {
            assertThat(event.rideId()).isEqualTo(rideId);
            assertThat(event.driverId()).isEqualTo(driverId);
        });
    }

    @Test
    void acceptThrowEntityNotFoundException() {
        var rideId = 1L;
        var driverId = 2L;
        var carId = 3L;

        var driverInfo = Mockito.mock(DriverInfo.class);
        Mockito.when(driverInfoService.get(driverId)).thenReturn(driverInfo);
        Mockito.when(driverInfo.getCarId()).thenReturn(carId);
        Mockito.when(driverInfo.getStatus()).thenReturn(DriverStatus.FREE);

        Mockito.when(repository.findById(rideId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rideService.accept(rideId, driverId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Ride with id " + rideId + " not found");

        Mockito.verify(repository).findById(rideId);
    }


    @ParameterizedTest
    @EnumSource(value = RideStatus.class, names = {"CREATED"}, mode = EnumSource.Mode.EXCLUDE)
    void acceptThrowIllegalRideStatusTransitionException(RideStatus rideStatus) {
        var rideId = 1L;
        var driverId = 2L;
        var carId = 3L;

        var driverInfo = Mockito.mock(DriverInfo.class);
        Mockito.when(driverInfoService.get(driverId)).thenReturn(driverInfo);
        Mockito.when(driverInfo.getCarId()).thenReturn(carId);
        Mockito.when(driverInfo.getStatus()).thenReturn(DriverStatus.FREE);

        var ride = Mockito.mock(Ride.class);
        Mockito.when(repository.findById(rideId)).thenReturn(Optional.of(ride));
        Mockito.when(ride.getStatus()).thenReturn(rideStatus);

        assertThatThrownBy(() -> rideService.accept(rideId, driverId))
                .isInstanceOf(IllegalRideStatusTransitionException.class)
                .hasMessage("Status transition from " + rideStatus
                        + " to " + RideStatus.ACCEPTED + " not allowed");

        Mockito.verify(ride, atLeastOnce()).getStatus();
    }

    @Test
    void acceptShouldNotProcessDriverInfoExceptions() {
        var rideId = 1L;
        var driverId = 2L;

        Mockito.when(driverInfoService.get(driverId)).thenThrow(new EntityNotFoundException(""));

        assertThatThrownBy(() -> rideService.accept(rideId, driverId))
                .isInstanceOf(EntityNotFoundException.class);
        Mockito.verify(driverInfoService).get(driverId);
    }

    @Test
    void acceptThrowExceptionWhenCarNotAttachedToDriver() {
        var rideId = 1L;
        var driverId = 2L;

        var driverInfo = Mockito.mock(DriverInfo.class);
        Mockito.when(driverInfoService.get(driverId)).thenReturn(driverInfo);
        Mockito.when(driverInfo.getCarId()).thenReturn(null);
        Mockito.lenient().when(driverInfo.getStatus()).thenReturn(DriverStatus.FREE);

        assertThatThrownBy(() -> rideService.accept(rideId, driverId))
                .isInstanceOf(IllegalRideStatusTransitionException.class)
                .hasMessage("Driver with id " + driverId + " doesn't have a car");
        Mockito.verify(driverInfo).getCarId();
    }

    @ParameterizedTest
    @EnumSource(value = DriverStatus.class, names = {"FREE"}, mode = EnumSource.Mode.EXCLUDE)
    void acceptThrowExceptionWhenDriverIsNotFree(DriverStatus driverStatus) {
        var rideId = 1L;
        var driverId = 2L;
        var carId = 3L;

        var driverInfo = Mockito.mock(DriverInfo.class);
        Mockito.when(driverInfoService.get(driverId)).thenReturn(driverInfo);
        Mockito.lenient().when(driverInfo.getCarId()).thenReturn(carId);
        Mockito.when(driverInfo.getStatus()).thenReturn(driverStatus);

        assertThatThrownBy(() -> rideService.accept(rideId, driverId))
                .isInstanceOf(IllegalRideStatusTransitionException.class)
                .hasMessage("Driver with id " + driverId + " already on trip");
        Mockito.verify(driverInfo).getStatus();
    }

    @ParameterizedTest
    @EnumSource(value = RideStatus.class, names = {"ACCEPTED"})
    void driveToPassengerShouldWorkForCorrectStatusTransitions(RideStatus rideStatus) {
        var rideId = 1L;

        var ride = Mockito.mock(Ride.class);
        Mockito.when(repository.findById(rideId)).thenReturn(Optional.of(ride));
        Mockito.when(ride.getStatus()).thenReturn(rideStatus);

        Mockito.when(repository.save(ride)).thenReturn(ride);

        var rideResponse = Mockito.mock(RideResponse.class);
        Mockito.when(mapper.toResponse(ride)).thenReturn(rideResponse);

        //WHEN
        var result = rideService.driveToPassenger(rideId);

        //THEN
        assertThat(result).isEqualTo(rideResponse);
        Mockito.verify(ride).getStatus();
        Mockito.verify(ride).setStatus(RideStatus.DRIVING_TO_PASSENGER);
        Mockito.verify(repository).save(ride);
    }

    @Test
    void driveToPassengerThrowEntityNotFoundException() {
        var rideId = 1L;
        Mockito.when(repository.findById(rideId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rideService.driveToPassenger(rideId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Ride with id " + rideId + " not found");

        Mockito.verify(repository).findById(rideId);
    }

    @ParameterizedTest
    @EnumSource(value = RideStatus.class, names = {"ACCEPTED"}, mode = EnumSource.Mode.EXCLUDE)
    void driveToPassengerThrowIllegalRideStatusTransitionException(RideStatus rideStatus) {
        var rideId = 1L;

        var ride = Mockito.mock(Ride.class);
        Mockito.when(repository.findById(rideId)).thenReturn(Optional.of(ride));
        Mockito.when(ride.getStatus()).thenReturn(rideStatus);

        assertThatThrownBy(() -> rideService.driveToPassenger(rideId))
                .isInstanceOf(IllegalRideStatusTransitionException.class)
                .hasMessage("Status transition from " + rideStatus
                        + " to " + RideStatus.DRIVING_TO_PASSENGER + " not allowed");

        Mockito.verify(ride, atLeastOnce()).getStatus();
    }

    @ParameterizedTest
    @EnumSource(value = RideStatus.class, names = {"DRIVING_TO_PASSENGER"})
    void driveToDestinationShouldWorkForCorrectStatusTransitions(RideStatus rideStatus) {
        var rideId = 1L;

        var ride = Mockito.mock(Ride.class);
        Mockito.when(repository.findById(rideId)).thenReturn(Optional.of(ride));
        Mockito.when(ride.getStatus()).thenReturn(rideStatus);

        Mockito.when(repository.save(ride)).thenReturn(ride);

        var rideResponse = Mockito.mock(RideResponse.class);
        Mockito.when(mapper.toResponse(ride)).thenReturn(rideResponse);

        //WHEN
        var result = rideService.driveToDestination(rideId);

        //THEN
        assertThat(result).isEqualTo(rideResponse);
        Mockito.verify(ride).getStatus();
        Mockito.verify(ride).setStatus(RideStatus.DRIVING_TO_DESTINATION);
        Mockito.verify(repository).save(ride);
    }

    @Test
    void driveToDestinationThrowEntityNotFoundException() {
        var rideId = 1L;
        Mockito.when(repository.findById(rideId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rideService.driveToDestination(rideId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Ride with id " + rideId + " not found");

        Mockito.verify(repository).findById(rideId);
    }

    @ParameterizedTest
    @EnumSource(value = RideStatus.class, names = {"DRIVING_TO_PASSENGER"}, mode = EnumSource.Mode.EXCLUDE)
    void driveToDestinationThrowIllegalRideStatusTransitionException(RideStatus rideStatus) {
        var rideId = 1L;

        var ride = Mockito.mock(Ride.class);
        Mockito.when(repository.findById(rideId)).thenReturn(Optional.of(ride));
        Mockito.when(ride.getStatus()).thenReturn(rideStatus);

        assertThatThrownBy(() -> rideService.driveToDestination(rideId))
                .isInstanceOf(IllegalRideStatusTransitionException.class)
                .hasMessage("Status transition from " + rideStatus
                        + " to " + RideStatus.DRIVING_TO_DESTINATION + " not allowed");

        Mockito.verify(ride, atLeastOnce()).getStatus();
    }

    @ParameterizedTest
    @EnumSource(value = RideStatus.class, names = {"DRIVING_TO_DESTINATION"})
    void completeShouldWorkForCorrectStatusTransitions(RideStatus rideStatus) {
        //GIVEN
        var rideId = 1L;
        var driverId = 2L;

        var ride = Mockito.mock(Ride.class);
        Mockito.when(ride.getDriverId()).thenReturn(driverId);
        Mockito.when(repository.findById(rideId)).thenReturn(Optional.of(ride));
        Mockito.when(ride.getStatus()).thenReturn(rideStatus);

        Mockito.when(repository.save(ride)).thenReturn(ride);

        ArgumentCaptor<RideCompletedEvent> eventCaptor = ArgumentCaptor.forClass(RideCompletedEvent.class);

        var rideResponse = Mockito.mock(RideResponse.class);
        Mockito.when(mapper.toResponse(ride)).thenReturn(rideResponse);

        //WHEN
        var result = rideService.complete(rideId);

        //THEN
        assertThat(result).isEqualTo(rideResponse);
        Mockito.verify(ride).getStatus();
        Mockito.verify(ride).setStatus(RideStatus.COMPLETED);
        Mockito.verify(ride).getDriverId();
        Mockito.verify(driverInfoService).changeDriverStatus(driverId, DriverStatus.FREE);

        Mockito.verify(eventPublisher).publishEvent(eventCaptor.capture());
        RideCompletedEvent rideAcceptedEvent = eventCaptor.getValue();
        assertThat(rideAcceptedEvent).satisfies(event -> {
            assertThat(event.rideId()).isEqualTo(rideId);
        });
    }

    @Test
    void completeThrowEntityNotFoundException() {
        var rideId = 1L;
        Mockito.when(repository.findById(rideId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rideService.complete(rideId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Ride with id " + rideId + " not found");

        Mockito.verify(repository).findById(rideId);
    }

    @ParameterizedTest
    @EnumSource(value = RideStatus.class, names = {"DRIVING_TO_DESTINATION"}, mode = EnumSource.Mode.EXCLUDE)
    void completeThrowIllegalRideStatusTransitionException(RideStatus rideStatus) {
        var rideId = 1L;

        var ride = Mockito.mock(Ride.class);
        Mockito.when(repository.findById(rideId)).thenReturn(Optional.of(ride));
        Mockito.when(ride.getStatus()).thenReturn(rideStatus);

        assertThatThrownBy(() -> rideService.complete(rideId))
                .isInstanceOf(IllegalRideStatusTransitionException.class)
                .hasMessage("Status transition from " + rideStatus
                        + " to " + RideStatus.COMPLETED + " not allowed");

        Mockito.verify(ride, atLeastOnce()).getStatus();
    }

    @ParameterizedTest
    @EnumSource(value = RideStatus.class, names = {"CREATED", "ACCEPTED", "DRIVING_TO_PASSENGER"})
    void cancelShouldWorkForCorrectStatusTransitions(RideStatus rideStatus) {
        //GIVEN
        var rideId = 1L;
        var driverId = 2L;

        var ride = Mockito.mock(Ride.class);
        Mockito.when(ride.getDriverId()).thenReturn(rideStatus != RideStatus.CREATED ? driverId : null);
        Mockito.when(repository.findById(rideId)).thenReturn(Optional.of(ride));
        Mockito.when(ride.getStatus()).thenReturn(rideStatus);

        Mockito.when(repository.save(ride)).thenReturn(ride);

        ArgumentCaptor<RideCanceledEvent> eventCaptor = ArgumentCaptor.forClass(RideCanceledEvent.class);

        var rideResponse = Mockito.mock(RideResponse.class);
        Mockito.when(mapper.toResponse(ride)).thenReturn(rideResponse);

        //WHEN
        var result = rideService.cancel(rideId);

        //THEN
        assertThat(result).isEqualTo(rideResponse);
        Mockito.verify(ride).getStatus();
        if (rideStatus != RideStatus.CREATED) {
            Mockito.verify(driverInfoService).changeDriverStatus(driverId, DriverStatus.FREE);
        }
        Mockito.verify(ride, atLeastOnce()).getDriverId();
        Mockito.verify(ride).setStatus(RideStatus.CANCELED);

        Mockito.verify(eventPublisher).publishEvent(eventCaptor.capture());
        RideCanceledEvent rideCanceledEvent = eventCaptor.getValue();
        assertThat(rideCanceledEvent).satisfies(event -> {
            assertThat(event.rideId()).isEqualTo(rideId);
        });
    }

    @Test
    void cancelThrowEntityNotFoundException() {
        var rideId = 1L;
        Mockito.when(repository.findById(rideId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rideService.cancel(rideId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Ride with id " + rideId + " not found");

        Mockito.verify(repository).findById(rideId);
    }

    @ParameterizedTest
    @EnumSource(value = RideStatus.class, names = {"CREATED", "ACCEPTED", "DRIVING_TO_PASSENGER"}, mode = EnumSource.Mode.EXCLUDE)
    void cancelThrowIllegalRideStatusTransitionException(RideStatus rideStatus) {
        var rideId = 1L;

        var ride = Mockito.mock(Ride.class);
        Mockito.when(repository.findById(rideId)).thenReturn(Optional.of(ride));
        Mockito.when(ride.getStatus()).thenReturn(rideStatus);

        assertThatThrownBy(() -> rideService.cancel(rideId))
                .isInstanceOf(IllegalRideStatusTransitionException.class)
                .hasMessage("Status transition from " + rideStatus
                        + " to " + RideStatus.CANCELED + " not allowed");

        Mockito.verify(ride, atLeastOnce()).getStatus();
    }
}