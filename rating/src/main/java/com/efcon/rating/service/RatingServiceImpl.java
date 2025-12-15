package com.efcon.rating.service;

import com.efcon.rating.dto.RatingRequest;
import com.efcon.rating.dto.RatingResponse;
import com.efcon.rating.exception.DocumentNotFoundException;
import com.efcon.rating.mapper.RatingMapper;
import com.efcon.rating.model.Rating;
import com.efcon.rating.repository.RatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {
    private final RatingRepository repository;
    private final RatingMapper mapper;

    @Override
    public RatingResponse getRating(Long id) {
        Rating rating = repository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException("Rating with id " + id + " not found"));
        return mapper.toResponse(rating);
    }

    @Override
    public List<RatingResponse> getAllRatings() {
        return mapper.toResponseList(repository.findAll());
    }

    @Override
    public void deleteRating(Long id) {
        repository.deleteById(id);
    }

    @Override
    public RatingResponse putPassengerRating(Long id, RatingRequest passengerRating) {
        Rating rating = repository.findById(id).orElse(new Rating(id));
        mapper.updatePassengerRating(passengerRating, rating);
        return mapper.toResponse(repository.save(rating));
    }

    @Override
    public RatingResponse putDriverRating(Long id, RatingRequest driverRating) {
        Rating rating = repository.findById(id).orElse(new Rating(id));
        mapper.updateDriverRating(driverRating, rating);
        return mapper.toResponse(repository.save(rating));
    }

    @Override
    public void deletePassengerRating(Long id) {
        Rating rating = repository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException("Rating with id " + id + " not found"));
        repository.save(mapper.flushPassengerRating(rating));
    }

    @Override
    public void deleteDriverRating(Long id) {
        Rating rating = repository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException("Rating with id " + id + " not found"));
        repository.save(mapper.flushDriverRating(rating));
    }
}
