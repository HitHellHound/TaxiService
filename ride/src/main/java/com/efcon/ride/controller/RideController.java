package com.efcon.ride.controller;

import com.efcon.ride.dto.RideRequestDTO;
import com.efcon.ride.dto.RideResponseDTO;
import com.efcon.ride.service.CRUDService;
import com.efcon.ride.service.RideService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rides")
@RequiredArgsConstructor
public class RideController extends AbstractCRUDController<RideRequestDTO, RideResponseDTO> {
    private final RideService service;

    @Override
    protected CRUDService<RideRequestDTO, RideResponseDTO> getService() {
        return service;
    }
}
