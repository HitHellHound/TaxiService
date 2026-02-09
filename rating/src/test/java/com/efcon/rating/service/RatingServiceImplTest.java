package com.efcon.rating.service;

import com.efcon.rating.dto.RatingRequest;
import com.efcon.rating.dto.RatingResponse;
import com.efcon.rating.dto.RideResponse;
import com.efcon.rating.dto.RideStatus;
import com.efcon.rating.exception.DocumentNotFoundException;
import com.efcon.rating.exception.EntityNotFoundException;
import com.efcon.rating.exception.IllegalRatingCreationException;
import com.efcon.rating.mapper.RatingMapper;
import com.efcon.rating.model.Rating;
import com.efcon.rating.repository.RatingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class RatingServiceImplTest {
    @Mock
    private RideService rideService;
    @Mock
    private RatingRepository repository;
    @Mock
    private RatingMapper mapper;

    @InjectMocks
    private RatingServiceImpl ratingService;

    @Test
    void getAllShouldUseRepositoryFindAll() {
        var rating = Mockito.mock(Rating.class);
        var ratingList = List.of(rating);
        Mockito.when(repository.findAll()).thenReturn(ratingList);

        var ratingResponse =  Mockito.mock(RatingResponse.class);
        var ratingResponseList = List.of(ratingResponse);
        Mockito.when(mapper.toResponseList(ratingList)).thenReturn(ratingResponseList);

        var result = ratingService.getAllRatings();

        assertThat(result)
                .isNotEmpty()
                .isEqualTo(ratingResponseList);
        Mockito.verify(repository).findAll();
    }

    @Test
    void getShouldUseRepositoryFindById() {
        var ratingId = 1L;
        var rating = Mockito.mock(Rating.class);
        Mockito.when(repository.findById(ratingId)).thenReturn(Optional.of(rating));

        var ratingResponse =  Mockito.mock(RatingResponse.class);
        Mockito.when(mapper.toResponse(rating)).thenReturn(ratingResponse);

        var result = ratingService.getRating(ratingId);

        assertThat(result).isEqualTo(ratingResponse);
        Mockito.verify(repository).findById(ratingId);
    }

    @Test
    void getThrowDocumentNotFoundException() {
        var ratingId = 1L;
        Mockito.when(repository.findById(ratingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ratingService.getRating(ratingId))
                .isInstanceOf(DocumentNotFoundException.class)
                .hasMessage("Rating with id " + ratingId + " not found");
        Mockito.verify(repository).findById(ratingId);
    }

    @Test
    void deleteShouldUseRepositoryDeleteById() {
        var ratingId = 1L;

        ratingService.deleteRating(ratingId);

        Mockito.verify(repository).deleteById(ratingId);
    }

    @ParameterizedTest
    @EnumSource(value = RideStatus.class, names = {"CANCELED", "COMPLETED"})
    void putPassengerRatingShouldUseRideServiceAndRepository(RideStatus rideStatus) {
        var rideId = 1L;
        var passengerRating = Mockito.mock(RatingRequest.class);
        var ride = Mockito.mock(RideResponse.class);
        Mockito.when(rideService.get(rideId)).thenReturn(ride);
        Mockito.when(ride.status()).thenReturn(rideStatus);

        var rating = Mockito.mock(Rating.class);
        Mockito.when(repository.findById(rideId)).thenReturn(Optional.of(rating));
        Mockito.when(repository.save(rating)).thenReturn(rating);

        var ratingResponse =  Mockito.mock(RatingResponse.class);
        Mockito.when(mapper.toResponse(rating)).thenReturn(ratingResponse);

        var result = ratingService.putPassengerRating(rideId, passengerRating);

        assertThat(result).isEqualTo(ratingResponse);
        Mockito.verify(rideService).get(rideId);
        Mockito.verify(ride).status();
        Mockito.verify(repository).findById(rideId);
        Mockito.verify(mapper).updatePassengerRating(passengerRating, rating);
        Mockito.verify(repository).save(rating);
    }

    @Test
    void putPassengerRatingShouldNotProcessRideServiceExceptions() {
        var rideId = 1L;
        var passengerRating = Mockito.mock(RatingRequest.class);
        Mockito.when(rideService.get(rideId))
                .thenThrow(new EntityNotFoundException(""));

        assertThatThrownBy(() -> ratingService.putPassengerRating(rideId, passengerRating))
                .isInstanceOf(EntityNotFoundException.class);
        Mockito.verify(rideService).get(rideId);
    }

    @ParameterizedTest
    @EnumSource(value = RideStatus.class, names = {"CANCELED", "COMPLETED"}, mode = EnumSource.Mode.EXCLUDE)
    void putPassengerRatingThrowIllegalRatingCreationException(RideStatus rideStatus) {
        var rideId = 1L;
        var passengerRating = Mockito.mock(RatingRequest.class);
        var ride = Mockito.mock(RideResponse.class);
        Mockito.when(rideService.get(rideId)).thenReturn(ride);
        Mockito.when(ride.status()).thenReturn(rideStatus);

        assertThatThrownBy(() -> ratingService.putPassengerRating(rideId, passengerRating))
                .isInstanceOf(IllegalRatingCreationException.class)
                .hasMessage("Can't create rating for ride in " + rideStatus + " status");
        Mockito.verify(ride, Mockito.atLeastOnce()).status();
    }

    @ParameterizedTest
    @EnumSource(value = RideStatus.class, names = {"CANCELED", "COMPLETED"})
    void putPassengerRatingShouldCreateNewRatingIfNotExist(RideStatus rideStatus) {
        var rideId = 1L;
        var passengerRating = Mockito.mock(RatingRequest.class);
        var ride = Mockito.mock(RideResponse.class);
        Mockito.when(rideService.get(rideId)).thenReturn(ride);
        Mockito.when(ride.status()).thenReturn(rideStatus);

        Mockito.when(repository.findById(rideId)).thenReturn(Optional.empty());
        var rating = Mockito.mock(Rating.class);
        Mockito.when(repository.save(any(Rating.class))).thenReturn(rating);

        var newRatingCaptor = ArgumentCaptor.forClass(Rating.class);

        var ratingResponse =  Mockito.mock(RatingResponse.class);
        Mockito.when(mapper.toResponse(rating)).thenReturn(ratingResponse);

        var result = ratingService.putPassengerRating(rideId, passengerRating);

        assertThat(result).isEqualTo(ratingResponse);
        Mockito.verify(rideService).get(rideId);
        Mockito.verify(ride).status();
        Mockito.verify(repository).findById(rideId);
        Mockito.verify(mapper).updatePassengerRating(eq(passengerRating), newRatingCaptor.capture());
        Mockito.verify(repository).save(any(Rating.class));

        assertThat(newRatingCaptor.getValue())
                .extracting(Rating::getId)
                .isEqualTo(rideId);
    }

    @ParameterizedTest
    @EnumSource(value = RideStatus.class, names = {"CANCELED", "COMPLETED"})
    void putDriverRatingShouldUseRideServiceAndRepository(RideStatus rideStatus) {
        var rideId = 1L;
        var driverRating = Mockito.mock(RatingRequest.class);
        var ride = Mockito.mock(RideResponse.class);
        Mockito.when(rideService.get(rideId)).thenReturn(ride);
        Mockito.when(ride.status()).thenReturn(rideStatus);

        var rating = Mockito.mock(Rating.class);
        Mockito.when(repository.findById(rideId)).thenReturn(Optional.of(rating));
        Mockito.when(repository.save(rating)).thenReturn(rating);

        var ratingResponse =  Mockito.mock(RatingResponse.class);
        Mockito.when(mapper.toResponse(rating)).thenReturn(ratingResponse);

        var result = ratingService.putDriverRating(rideId, driverRating);

        assertThat(result).isEqualTo(ratingResponse);
        Mockito.verify(rideService).get(rideId);
        Mockito.verify(ride).status();
        Mockito.verify(repository).findById(rideId);
        Mockito.verify(mapper).updateDriverRating(driverRating, rating);
        Mockito.verify(repository).save(rating);
    }

    @Test
    void putDriverRatingShouldNotProcessRideServiceExceptions() {
        var rideId = 1L;
        var driverRating = Mockito.mock(RatingRequest.class);
        Mockito.when(rideService.get(rideId))
                .thenThrow(new EntityNotFoundException(""));

        assertThatThrownBy(() -> ratingService.putPassengerRating(rideId, driverRating))
                .isInstanceOf(EntityNotFoundException.class);
        Mockito.verify(rideService).get(rideId);
    }

    @ParameterizedTest
    @EnumSource(value = RideStatus.class, names = {"CANCELED", "COMPLETED"}, mode = EnumSource.Mode.EXCLUDE)
    void putDriverRatingThrowIllegalRatingCreationException(RideStatus rideStatus) {
        var rideId = 1L;
        var driverRating = Mockito.mock(RatingRequest.class);
        var ride = Mockito.mock(RideResponse.class);
        Mockito.when(rideService.get(rideId)).thenReturn(ride);
        Mockito.when(ride.status()).thenReturn(rideStatus);

        assertThatThrownBy(() -> ratingService.putDriverRating(rideId, driverRating))
                .isInstanceOf(IllegalRatingCreationException.class)
                .hasMessage("Can't create rating for ride in " + rideStatus + " status");
        Mockito.verify(ride, Mockito.atLeastOnce()).status();
    }

    @ParameterizedTest
    @EnumSource(value = RideStatus.class, names = {"CANCELED", "COMPLETED"})
    void putDriverRatingThrowIllegalRatingCreationExceptionIfDriverNotAssigned(RideStatus rideStatus) {
        var rideId = 1L;
        var driverRating = Mockito.mock(RatingRequest.class);
        var ride = Mockito.mock(RideResponse.class);
        Mockito.when(ride.id()).thenReturn(rideId);
        Mockito.when(rideService.get(rideId)).thenReturn(ride);
        Mockito.when(ride.status()).thenReturn(rideStatus);
        Mockito.when(ride.driverId()).thenReturn(null);

        assertThatThrownBy(() -> ratingService.putDriverRating(rideId, driverRating))
                .isInstanceOf(IllegalRatingCreationException.class)
                .hasMessage("Can't create driver's rating cause ride with id " + rideId
                        + " doesn't have bounded driver");
        Mockito.verify(ride, Mockito.atLeastOnce()).status();
    }

    @ParameterizedTest
    @EnumSource(value = RideStatus.class, names = {"CANCELED", "COMPLETED"})
    void putDriverRatingShouldCreateNewRatingIfNotExist(RideStatus rideStatus) {
        var rideId = 1L;
        var driverRating = Mockito.mock(RatingRequest.class);
        var ride = Mockito.mock(RideResponse.class);
        Mockito.when(rideService.get(rideId)).thenReturn(ride);
        Mockito.when(ride.status()).thenReturn(rideStatus);

        Mockito.when(repository.findById(rideId)).thenReturn(Optional.empty());
        var rating = Mockito.mock(Rating.class);
        Mockito.when(repository.save(any(Rating.class))).thenReturn(rating);

        var newRatingCaptor = ArgumentCaptor.forClass(Rating.class);

        var ratingResponse =  Mockito.mock(RatingResponse.class);
        Mockito.when(mapper.toResponse(rating)).thenReturn(ratingResponse);

        var result = ratingService.putDriverRating(rideId, driverRating);

        assertThat(result).isEqualTo(ratingResponse);
        Mockito.verify(rideService).get(rideId);
        Mockito.verify(ride).status();
        Mockito.verify(repository).findById(rideId);
        Mockito.verify(mapper).updateDriverRating(eq(driverRating), newRatingCaptor.capture());
        Mockito.verify(repository).save(any(Rating.class));

        assertThat(newRatingCaptor.getValue())
                .extracting(Rating::getId)
                .isEqualTo(rideId);
    }

    @Test
    void deletePassengerRatingShouldUseRepositorySave() {
        var ratingId = 1L;
        var rating = Mockito.mock(Rating.class);
        Mockito.when(repository.findById(ratingId)).thenReturn(Optional.of(rating));

        var flushedRating =  Mockito.mock(Rating.class);
        Mockito.when(mapper.flushPassengerRating(rating)).thenReturn(flushedRating);

        ratingService.deletePassengerRating(ratingId);

        Mockito.verify(repository).findById(ratingId);
        Mockito.verify(mapper).flushPassengerRating(rating);
        Mockito.verify(repository).save(flushedRating);
    }

    @Test
    void deletePassengerRatingThrowDocumentNotFoundException() {
        var ratingId = 1L;
        Mockito.when(repository.findById(ratingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ratingService.deletePassengerRating(ratingId))
                .isInstanceOf(DocumentNotFoundException.class)
                .hasMessage("Rating with id " + ratingId + " not found");
        Mockito.verify(repository).findById(ratingId);
    }

    @Test
    void deleteDriverRatingShouldUseRepositorySave() {
        var ratingId = 1L;
        var rating = Mockito.mock(Rating.class);
        Mockito.when(repository.findById(ratingId)).thenReturn(Optional.of(rating));

        var flushedRating =  Mockito.mock(Rating.class);
        Mockito.when(mapper.flushDriverRating(rating)).thenReturn(flushedRating);

        ratingService.deleteDriverRating(ratingId);

        Mockito.verify(repository).findById(ratingId);
        Mockito.verify(mapper).flushDriverRating(rating);
        Mockito.verify(repository).save(flushedRating);
    }

    @Test
    void deleteDriverRatingThrowDocumentNotFoundException() {
        var ratingId = 1L;
        Mockito.when(repository.findById(ratingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ratingService.deleteDriverRating(ratingId))
                .isInstanceOf(DocumentNotFoundException.class)
                .hasMessage("Rating with id " + ratingId + " not found");
        Mockito.verify(repository).findById(ratingId);
    }
}