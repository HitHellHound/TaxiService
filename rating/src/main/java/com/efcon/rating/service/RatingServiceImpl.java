package com.efcon.rating.service;

import com.efcon.rating.dto.RatingRequest;
import com.efcon.rating.dto.RatingResponse;
import com.efcon.rating.dto.RideResponse;
import com.efcon.rating.dto.RideStatus;
import com.efcon.rating.exception.DocumentNotFoundException;
import com.efcon.rating.exception.IllegalRatingCreationException;
import com.efcon.rating.mapper.RatingMapper;
import com.efcon.rating.model.Rating;
import com.efcon.rating.repository.RatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {
    private final Set<RideStatus> NOT_ACTIVE_STATUSES = Set.of(RideStatus.CANCELED, RideStatus.COMPLETED);

    private final RideService rideService;
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
        RideResponse ride = rideService.get(id);

        if (!NOT_ACTIVE_STATUSES.contains(ride.status())) {
            throw new IllegalRatingCreationException("Can't create rating for ride in " + ride.status() + " status");
        }

        Rating rating = repository.findById(id).orElse(new Rating(id));
        mapper.updatePassengerRating(passengerRating, rating);
        return mapper.toResponse(repository.save(rating));
    }

    @Override
    public RatingResponse putDriverRating(Long id, RatingRequest driverRating) {
        RideResponse ride = rideService.get(id);

        if (!NOT_ACTIVE_STATUSES.contains(ride.status())) {
            throw new IllegalRatingCreationException("Can't create rating for ride in " + ride.status() + " status");
        }
        if (ride.driverId() == null) {
            throw new IllegalRatingCreationException("Can't create driver's rating cause ride with id " + ride.id()
                    + " doesn't have bounded driver");
        }

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
