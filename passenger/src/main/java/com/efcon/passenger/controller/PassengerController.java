package com.efcon.passenger.controller;

import com.efcon.passenger.dto.PassengerRequest;
import com.efcon.passenger.dto.PassengerResponse;
import com.efcon.passenger.service.PassengerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/passengers")
@RequiredArgsConstructor
public class PassengerController {
    private final PassengerService service;

    @GetMapping
    public List<PassengerResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public PassengerResponse getById(@PathVariable long id) {
        return service.get(id);
    }

    @PostMapping
    public ResponseEntity<PassengerResponse> create(@RequestBody @Valid PassengerRequest requestBody) {
        return ResponseEntity.status(201).body(service.create(requestBody));
    }

    @PutMapping("/{id}")
    public PassengerResponse update(@PathVariable long id, @RequestBody @Valid PassengerRequest requestBody) {
        return service.update(id, requestBody);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
