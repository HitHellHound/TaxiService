package com.efcon.ride.service;

import com.efcon.ride.dto.PassengerRequest;
import com.efcon.ride.dto.PassengerResponse;
import com.efcon.ride.external.client.PassengerClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PassengerServiceImpl implements PassengerService {
    private final PassengerClient passengerClient;

    @Override
    public List<PassengerResponse> getAll() {
        return passengerClient.getAll();
    }

    @Override
    public PassengerResponse get(Long id) {
        return passengerClient.get(id);
    }

    @Override
    public PassengerResponse create(PassengerRequest request) {
        return passengerClient.create(request);
    }

    @Override
    public PassengerResponse update(Long id, PassengerRequest request) {
        return passengerClient.update(id, request);
    }

    @Override
    public void delete(Long id) {
        passengerClient.delete(id);
    }
}
