package com.efcon.ride.external.client;

import com.efcon.ride.dto.DriverRequest;
import com.efcon.ride.dto.DriverResponse;
import com.efcon.ride.external.config.DriverClientConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "driver", path = "/api/v1/drivers", configuration = DriverClientConfiguration.class)
public interface DriverClient {
    @GetMapping
    List<DriverResponse> getAll();

    @GetMapping("/{id}")
    DriverResponse get(@PathVariable Long id);

    @PostMapping
    DriverResponse create(@RequestBody DriverRequest passenger);

    @PutMapping("/{id}")
    DriverResponse update(@PathVariable Long id, @RequestBody DriverRequest passenger);

    @DeleteMapping("/{id}")
    void delete(@PathVariable Long id);
}
