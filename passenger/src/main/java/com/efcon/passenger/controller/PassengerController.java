package com.efcon.passenger.controller;

import com.efcon.passenger.dto.PassengerRequestDTO;
import com.efcon.passenger.dto.PassengerResponseDTO;
import com.efcon.passenger.service.PassengerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/passengers")
@RequiredArgsConstructor
public class PassengerController extends AbstractCRUDController<PassengerRequestDTO, PassengerResponseDTO> {
    private final PassengerService service;

    @Override
    protected PassengerService getService() {
        return service;
    }
}
