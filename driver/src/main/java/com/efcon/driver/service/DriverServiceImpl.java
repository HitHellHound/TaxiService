package com.efcon.driver.service;

import com.efcon.driver.dto.DriverRequest;
import com.efcon.driver.dto.DriverResponse;
import com.efcon.driver.exception.EntityNotFoundException;
import com.efcon.driver.mapper.DriverMapper;
import com.efcon.driver.model.Car;
import com.efcon.driver.model.Driver;
import com.efcon.driver.repository.CarRepository;
import com.efcon.driver.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DriverServiceImpl implements DriverService {
    private final DriverRepository repository;
    private final CarRepository carRepository;
    private final DriverMapper mapper;

    @Override
    public List<DriverResponse> getAll() {
        return mapper.toResponseList(repository.findAll());
    }

    @Override
    public DriverResponse get(Long id) {
        Driver driver = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Driver with id " + id + " not found"));
        return mapper.toResponse(driver);
    }

    @Override
    public DriverResponse create(DriverRequest driverRequest) {
        Driver newEntity = mapper.fromRequest(driverRequest);
        return mapper.toResponse(repository.save(newEntity));
    }

    @Override
    public DriverResponse update(Long id, DriverRequest driverRequest) {
        Driver driver = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Driver with id " + id + " not found"));
        mapper.updateEntityFromRequest(driverRequest, driver);
        return mapper.toResponse(repository.save(driver));
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Override
    public DriverResponse attachCar(Long driverId, Long carId) {
        Driver driver = repository.findById(driverId)
                .orElseThrow(() -> new EntityNotFoundException("Driver  with id " + driverId + " not found"));
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new EntityNotFoundException("Car  with id " + carId + " not found"));
        driver.setCar(car);
        return mapper.toResponse(repository.save(driver));
    }

    @Override
    public void detachCar(Long driverId) {
        Driver driver = repository.findById(driverId)
                .orElseThrow(() -> new EntityNotFoundException("Driver  with id " + driverId + " not found"));
        driver.setCar(null);
        repository.save(driver);
    }
}
