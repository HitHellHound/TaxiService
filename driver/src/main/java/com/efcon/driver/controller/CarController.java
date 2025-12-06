package com.efcon.driver.controller;

import com.efcon.driver.dto.CarRequestDTO;
import com.efcon.driver.dto.CarResponseDTO;
import com.efcon.driver.service.CarService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cars")
@RequiredArgsConstructor
public class CarController extends AbstractCRUDController<CarRequestDTO, CarResponseDTO> {
    private final CarService service;

    @Override
    protected CarService getService() {
        return service;
    }
}
