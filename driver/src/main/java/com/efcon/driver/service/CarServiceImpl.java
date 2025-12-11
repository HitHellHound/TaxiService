package com.efcon.driver.service;

import com.efcon.driver.dto.CarRequest;
import com.efcon.driver.dto.CarResponse;
import com.efcon.driver.exception.EntityNotFoundException;
import com.efcon.driver.mapper.CarMapper;
import com.efcon.driver.model.Car;
import com.efcon.driver.repository.CarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CarServiceImpl implements CarService {
    private final CarRepository repository;
    private final CarMapper mapper;

    @Override
    public List<CarResponse> getAll() {
        return mapper.toResponseList(repository.findAll());
    }

    @Override
    public CarResponse get(Long id) {
        Car car = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Car with id " + id + " not found"));
        return mapper.toResponse(car);
    }

    @Override
    public CarResponse create(CarRequest carRequest) {
        Car newCar = mapper.fromRequest(carRequest);
        return mapper.toResponse(repository.save(newCar));
    }

    @Override
    public CarResponse update(Long id, CarRequest carRequest) {
        Car car = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Car with id " + id + " not found"));
        mapper.updateEntityFromRequest(carRequest, car);
        return mapper.toResponse(repository.save(car));
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
