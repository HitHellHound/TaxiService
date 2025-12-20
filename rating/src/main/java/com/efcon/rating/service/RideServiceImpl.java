package com.efcon.rating.service;

import com.efcon.rating.dto.RideResponse;
import com.efcon.rating.external.client.RideGrpcClient;
import com.efcon.rating.mapper.RideMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RideServiceImpl implements RideService {
    private final RideMapper rideMapper;
    private final RideGrpcClient client;

    @Override
    public RideResponse get(Long id) {
        return rideMapper.fromRPCDto(client.getRideById(id));
    }
}
