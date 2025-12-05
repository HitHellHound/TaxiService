package com.efcon.passenger.controller;

import com.efcon.passenger.dto.PassengerRequestDTO;
import com.efcon.passenger.dto.PassengerResponseDTO;
import com.efcon.passenger.service.PassengerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/passengers")
@RequiredArgsConstructor
public class PassengerController {
    private final PassengerService service;

    @GetMapping
    public List<PassengerResponseDTO> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public PassengerResponseDTO getById(@PathVariable long id) {
        return service.get(id);
    }

    @PostMapping
    public ResponseEntity<PassengerResponseDTO> create(@RequestBody PassengerRequestDTO passengerDTO) {
        return ResponseEntity.status(201).body(service.create(passengerDTO));
    }

    @PutMapping("/{id}")
    public PassengerResponseDTO update(@PathVariable long id, @RequestBody PassengerRequestDTO passengerDTO) {
        return service.update(id, passengerDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(exception = NoSuchElementException.class)
    public ResponseEntity<String> passengerNotFound(NoSuchElementException exception) {
        return ResponseEntity.status(404).body(exception.getMessage());
    }
}
