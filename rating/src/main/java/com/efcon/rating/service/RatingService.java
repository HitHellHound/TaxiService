package com.efcon.rating.service;

import com.efcon.rating.dto.RatingRequest;
import com.efcon.rating.dto.RatingResponse;

import java.util.List;

public interface RatingService {
    RatingResponse getRating(Long id);
    List<RatingResponse> getAllRatings();

    void deleteRating(Long id);

    RatingResponse putPassengerRating(Long id, RatingRequest passengerRating);
    RatingResponse putDriverRating(Long id, RatingRequest driverRating);

    void deletePassengerRating(Long id);
    void deleteDriverRating(Long id);
}
