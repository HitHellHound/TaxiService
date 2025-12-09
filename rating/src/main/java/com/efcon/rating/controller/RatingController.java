package com.efcon.rating.controller;

import com.efcon.rating.dto.RatingRequestDTO;
import com.efcon.rating.dto.RatingResponseDTO;
import com.efcon.rating.service.RatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/ratings")
@RequiredArgsConstructor
public class RatingController {
    private final RatingService service;

    @GetMapping
    public List<RatingResponseDTO> getAllRatings() {
        return service.getAllRatings();
    }

    @GetMapping("/{id}")
    public RatingResponseDTO getRating(@PathVariable long id) {
        return service.getRating(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRating(@PathVariable long id) {
        service.deleteRating(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/passenger-rating")
    public ResponseEntity<RatingResponseDTO> putPassengerRating(@PathVariable long id, @RequestBody RatingRequestDTO passengerRating) {
        return ResponseEntity.status(201).body(service.putPassengerRating(id, passengerRating));
    }

    @PutMapping("/{id}/driver-rating")
    public ResponseEntity<RatingResponseDTO> putDriverRating(@PathVariable long id, @RequestBody RatingRequestDTO driverRating) {
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

    @ExceptionHandler(exception = NoSuchElementException.class)
    public ResponseEntity<String> entityNotFound(NoSuchElementException exception) {
        return ResponseEntity.status(404).body(exception.getMessage());
    }
}
