package com.efcon.driver.controller;

import com.efcon.driver.dto.DriverRequestDTO;
import com.efcon.driver.dto.DriverResponseDTO;
import com.efcon.driver.service.CRUDService;
import com.efcon.driver.service.DriverService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/drivers")
@RequiredArgsConstructor
public class DriverController extends AbstractCRUDController<DriverRequestDTO, DriverResponseDTO> {
    private final DriverService service;

    @Override
    protected CRUDService<DriverRequestDTO, DriverResponseDTO> getService() {
        return service;
    }
}
