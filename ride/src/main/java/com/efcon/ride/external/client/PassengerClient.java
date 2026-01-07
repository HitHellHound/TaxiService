package com.efcon.ride.external.client;

import com.efcon.ride.dto.PassengerRequest;
import com.efcon.ride.dto.PassengerResponse;
import com.efcon.ride.external.config.PassengerClientConfiguration;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "passenger", path = "/api/v1/passengers", configuration = PassengerClientConfiguration.class)
@CircuitBreaker(name = "driver-service")
public interface PassengerClient {
    @GetMapping
    List<PassengerResponse> getAll();

    @GetMapping("/{id}")
    PassengerResponse get(@PathVariable Long id);

    @PostMapping
    PassengerResponse create(@RequestBody PassengerRequest passenger);

    @PutMapping("/{id}")
    PassengerResponse update(@PathVariable Long id, @RequestBody PassengerRequest passenger);

    @DeleteMapping("/{id}")
    void delete(@PathVariable Long id);
}
