package com.efcon.driver.service;

import com.efcon.driver.dto.DriverRequestDTO;
import com.efcon.driver.dto.DriverResponseDTO;
import com.efcon.driver.mapper.DriverMapper;
import com.efcon.driver.model.Car;
import com.efcon.driver.model.Driver;
import com.efcon.driver.repository.CarRepository;
import com.efcon.driver.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class DriverServiceImpl extends AbstractCRUDService<Driver, DriverRequestDTO, DriverResponseDTO> implements DriverService {
    private final DriverRepository repository;
    private final CarRepository carRepository;
    private final DriverMapper mapper;

    @Override
    public DriverResponseDTO attachCar(Long driverId, Long carId) {
        Driver driver = repository.findById(driverId)
                .orElseThrow(() -> new NoSuchElementException("Driver  with id " + driverId + " not found"));
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new NoSuchElementException("Car  with id " + carId + " not found"));
        driver.setCar(car);
        return mapper.toResponseDto(repository.save(driver));
    }

    @Override
    public void detachCar(Long driverId) {
        Driver driver = repository.findById(driverId)
                .orElseThrow(() -> new NoSuchElementException("Driver  with id " + driverId + " not found"));
        driver.setCar(null);
        repository.save(driver);
    }

    @Override
    protected DriverRepository getRepository() {
        return repository;
    }

    @Override
    protected DriverMapper getMapper() {
        return mapper;
    }

    @Override
    protected String getEntityName() {
        return "Driver";
    }
}
