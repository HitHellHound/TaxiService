package com.efcon.rating.service;

import com.efcon.rating.dto.RatingRequestDTO;
import com.efcon.rating.dto.RatingResponseDTO;
import com.efcon.rating.mapper.RatingMapper;
import com.efcon.rating.model.Rating;
import com.efcon.rating.repository.RatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {
    private final RatingRepository repository;
    private final RatingMapper mapper;

    @Override
    public RatingResponseDTO getRating(Long id) {
        Rating rating = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Rating with id " + id + " not found"));
        return mapper.toResponseDto(rating);
    }

    @Override
    public List<RatingResponseDTO> getAllRatings() {
        return mapper.toResponseDtoList(repository.findAll());
    }

    @Override
    public void deleteRating(Long id) {
        repository.deleteById(id);
    }

    @Override
    public RatingResponseDTO putPassengerRating(Long id, RatingRequestDTO passengerRating) {
        Rating rating = repository.findById(id).orElse(new Rating(id));
        mapper.updatePassengerRating(passengerRating, rating);
        return mapper.toResponseDto(repository.save(rating));
    }

    @Override
    public RatingResponseDTO putDriverRating(Long id, RatingRequestDTO driverRating) {
        Rating rating = repository.findById(id).orElse(new Rating(id));
        mapper.updateDriverRating(driverRating, rating);
        return mapper.toResponseDto(repository.save(rating));
    }

    @Override
    public void deletePassengerRating(Long id) {
        Rating rating = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Rating with id " + id + " not found"));
        repository.save(mapper.flushPassengerRating(rating));
    }

    @Override
    public void deleteDriverRating(Long id) {
        Rating rating = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Rating with id " + id + " not found"));
        repository.save(mapper.flushDriverRating(rating));
    }
}
