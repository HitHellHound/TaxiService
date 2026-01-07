package com.efcon.rating.external.client;

import com.efcon.rating.exception.EntityNotFoundException;
import com.efcon.rating.exception.ExternalBadRequestException;
import com.efcon.rating.exception.ExternalServiceException;
import com.efcon.ride.grpc.stubs.GetRideByIdRequest;
import com.efcon.ride.grpc.stubs.Ride;
import com.efcon.ride.grpc.stubs.RideGRPCServiceGrpc;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@CircuitBreaker(name = "ride-service")
public class RideGrpcClientImpl implements RideGrpcClient {
    @GrpcClient("ride-service")
    private RideGRPCServiceGrpc.RideGRPCServiceBlockingStub client;

    @Override
    public Ride getRideById(Long id) {
        GetRideByIdRequest request = GetRideByIdRequest.newBuilder()
                .setId(id)
                .build();
        try {
            return client.getRideById(request).getRide();
        } catch (StatusRuntimeException exception) {
            throw toServiceException(exception);
        }
    }

    private RuntimeException toServiceException(StatusRuntimeException exception) {
        return switch (exception.getStatus().getCode()) {
            case NOT_FOUND -> new EntityNotFoundException(exception.getMessage());
            case INVALID_ARGUMENT -> new ExternalBadRequestException(exception.getMessage());
            case UNAVAILABLE -> new ExternalServiceException("Ride service can't process request");
            default -> new RuntimeException(exception);
        };
    }
}
