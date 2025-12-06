package com.efcon.driver.service;

import com.efcon.driver.dto.DriverRequestDTO;
import com.efcon.driver.dto.DriverResponseDTO;
import com.efcon.driver.mapper.DriverMapper;
import com.efcon.driver.model.Driver;
import com.efcon.driver.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DriverServiceImpl extends AbstractCRUDService<Driver, DriverRequestDTO, DriverResponseDTO> implements DriverService {
    private final DriverRepository repository;
    private final DriverMapper mapper;

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
