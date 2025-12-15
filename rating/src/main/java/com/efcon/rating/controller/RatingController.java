package com.efcon.rating.controller;

import com.efcon.rating.dto.RatingRequest;
import com.efcon.rating.dto.RatingResponse;
import com.efcon.rating.service.RatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ratings")
@RequiredArgsConstructor
public class RatingController {
    private final RatingService service;

    @GetMapping
    public List<RatingResponse> getAllRatings() {
        return service.getAllRatings();
    }

    @GetMapping("/{id}")
    public RatingResponse getRating(@PathVariable long id) {
        return service.getRating(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRating(@PathVariable long id) {
        service.deleteRating(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/passenger-rating")
    public ResponseEntity<RatingResponse> putPassengerRating(@PathVariable long id, @RequestBody @Valid RatingRequest passengerRating) {
        return ResponseEntity.status(201).body(service.putPassengerRating(id, passengerRating));
    }

    @PutMapping("/{id}/driver-rating")
    public ResponseEntity<RatingResponse> putDriverRating(@PathVariable long id, @RequestBody @Valid RatingRequest driverRating) {
        return ResponseEntity.status(201).body(service.putDriverRating(id, driverRating));
    }

    @DeleteMapping("/{id}/passenger-rating")
    public ResponseEntity<Void> deletePassengerRating(@PathVariable long id) {
        service.deletePassengerRating(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/driver-rating")
    public ResponseEntity<Void> deleteDriverRating(@PathVariable long id) {
        service.deleteDriverRating(id);
        return ResponseEntity.noContent().build();
    }
}
