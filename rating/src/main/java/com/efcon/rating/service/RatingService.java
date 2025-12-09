package com.efcon.rating.service;

import com.efcon.rating.dto.RatingRequestDTO;
import com.efcon.rating.dto.RatingResponseDTO;

import java.util.List;

public interface RatingService {
    RatingResponseDTO getRating(Long id);
    List<RatingResponseDTO> getAllRatings();

    void deleteRating(Long id);

    RatingResponseDTO putPassengerRating(Long id, RatingRequestDTO passengerRating);
    RatingResponseDTO putDriverRating(Long id, RatingRequestDTO driverRating);

    void deletePassengerRating(Long id);
    void deleteDriverRating(Long id);
}
