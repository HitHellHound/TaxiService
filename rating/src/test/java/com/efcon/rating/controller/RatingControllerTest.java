package com.efcon.rating.controller;

import com.efcon.rating.dto.RatingRequest;
import com.efcon.rating.dto.RatingResponse;
import com.efcon.rating.exception.EntityNotFoundException;
import com.efcon.rating.service.RatingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class RatingControllerTest {
    @Mock
    private RatingService ratingService;

    @InjectMocks
    private RatingController ratingController;

    @Test
    void getAllRatingsShouldUseratingService() {
        var rating = Mockito.mock(RatingResponse.class);
        var ratingList = List.of(rating);
        Mockito.when(ratingService.getAllRatings()).thenReturn(ratingList);

        var result = ratingController.getAllRatings();

        assertThat(result).isEqualTo(ratingList);
        Mockito.verify(ratingService).getAllRatings();
    }

    @Test
    void getRatingShouldUseRatingService() {
        var ratingId = 1L;
        var rating = Mockito.mock(RatingResponse.class);
        Mockito.when(ratingService.getRating(ratingId)).thenReturn(rating);

        var result = ratingController.getRating(ratingId);

        assertThat(result).isEqualTo(rating);
        Mockito.verify(ratingService).getRating(ratingId);
    }

    @Test
    void getRatingShouldNotProcessEntityNotFoundException() {
        var ratingId = 1L;
        Mockito.when(ratingService.getRating(ratingId)).thenThrow(new EntityNotFoundException(""));

        assertThatThrownBy(() -> ratingController.getRating(ratingId))
                .isInstanceOf(EntityNotFoundException.class);
        Mockito.verify(ratingService).getRating(ratingId);
    }

    @Test
    void deleteShouldUseRatingServiceAndReturnNoContentStatusResponse() {
        var ratingId = 1L;

        var result = ratingController.deleteRating(ratingId);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(result.hasBody()).isFalse();
        Mockito.verify(ratingService).deleteRating(ratingId);
    }

    @Test
    void putPassengerRatingShouldUseServiceAndReturnCreatedStatus() {
        var ratingId = 1L;
        var passengerRating = Mockito.mock(RatingRequest.class);
        var rating = Mockito.mock(RatingResponse.class);
        Mockito.when(ratingService.putPassengerRating(ratingId, passengerRating)).thenReturn(rating);

        var result = ratingController.putPassengerRating(ratingId, passengerRating);

        assertThat(result)
                .isNotNull()
                .extracting(ResponseEntity::getStatusCode, ResponseEntity::getBody)
                .containsExactly(HttpStatus.CREATED, rating);
        Mockito.verify(ratingService).putPassengerRating(ratingId, passengerRating);
    }

    @Test
    void putPassengerRatingShouldNotProcessServiceExceptions() {
        var ratingId = 1L;
        var passengerRating = Mockito.mock(RatingRequest.class);
        Mockito.when(ratingService.putPassengerRating(ratingId, passengerRating))
                .thenThrow(new RuntimeException());

        assertThatThrownBy(() -> ratingController.putPassengerRating(ratingId, passengerRating));
        Mockito.verify(ratingService).putPassengerRating(ratingId, passengerRating);
    }

    @Test
    void putDriverRatingShouldUseServiceAndReturnCreatedStatus() {
        var ratingId = 1L;
        var driverRating = Mockito.mock(RatingRequest.class);
        var rating = Mockito.mock(RatingResponse.class);
        Mockito.when(ratingService.putDriverRating(ratingId, driverRating)).thenReturn(rating);

        var result = ratingController.putDriverRating(ratingId, driverRating);

        assertThat(result)
                .isNotNull()
                .extracting(ResponseEntity::getStatusCode, ResponseEntity::getBody)
                .containsExactly(HttpStatus.CREATED, rating);
        Mockito.verify(ratingService).putDriverRating(ratingId, driverRating);
    }

    @Test
    void putDriverRatingShouldNotProcessServiceExceptions() {
        var ratingId = 1L;
        var driverRating = Mockito.mock(RatingRequest.class);
        Mockito.when(ratingService.putDriverRating(ratingId, driverRating))
                .thenThrow(new RuntimeException());

        assertThatThrownBy(() -> ratingController.putDriverRating(ratingId, driverRating));
        Mockito.verify(ratingService).putDriverRating(ratingId, driverRating);
    }

    @Test
    void deletePassengerRatingShouldUseRatingServiceAndReturnNoContentStatusResponse() {
        var ratingId = 1L;

        var result = ratingController.deletePassengerRating(ratingId);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(result.hasBody()).isFalse();
        Mockito.verify(ratingService).deletePassengerRating(ratingId);
    }

    @Test
    void deleteDriverRatingShouldUseRatingServiceAndReturnNoContentStatusResponse() {
        var ratingId = 1L;

        var result = ratingController.deleteDriverRating(ratingId);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(result.hasBody()).isFalse();
        Mockito.verify(ratingService).deleteDriverRating(ratingId);
    }
}