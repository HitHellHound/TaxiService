package com.efcon.driver.service;

import com.efcon.driver.dto.CarRequestDTO;
import com.efcon.driver.dto.CarResponseDTO;
import com.efcon.driver.mapper.CarMapper;
import com.efcon.driver.model.Car;
import com.efcon.driver.repository.CarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CarServiceImpl extends AbstractCRUDService<Car, CarRequestDTO, CarResponseDTO> implements CarService {
    private final CarRepository repository;
    private final CarMapper mapper;

    @Override
    protected CarRepository getRepository() {
        return repository;
    }

    @Override
    protected CarMapper getMapper() {
        return mapper;
    }

    @Override
    protected String getEntityName() {
        return "Car";
    }
}
