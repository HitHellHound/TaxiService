package com.efcon.ride.service;

import com.efcon.ride.dto.DriverRequest;
import com.efcon.ride.dto.DriverResponse;
import com.efcon.ride.external.client.DriverClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DriverServiceImpl implements DriverService {
    private final DriverClient client;

    @Override
    public List<DriverResponse> getAll() {
        return client.getAll();
    }

    @Override
    public DriverResponse get(Long id) {
        return client.get(id);
    }

    @Override
    public DriverResponse create(DriverRequest request) {
        return client.create(request);
    }

    @Override
    public DriverResponse update(Long id, DriverRequest request) {
        return client.update(id, request);
    }

    @Override
    public void delete(Long id) {
        client.delete(id);
    }
}
