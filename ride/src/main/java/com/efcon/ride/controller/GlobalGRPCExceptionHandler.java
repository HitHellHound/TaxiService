package com.efcon.ride.controller;

import com.efcon.ride.exception.EntityNotFoundException;
import com.efcon.ride.exception.ExternalBadRequestException;
import com.efcon.ride.exception.ExternalServiceException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;

@GrpcAdvice
public class GlobalGRPCExceptionHandler {
    @GrpcExceptionHandler(EntityNotFoundException.class)
    public Status entityNotFound(EntityNotFoundException exception) {
        return Status.NOT_FOUND.withDescription(exception.getMessage());
    }

    @GrpcExceptionHandler(ExternalBadRequestException.class)
    public Status badRequest(ExternalBadRequestException exception) {
        return Status.INVALID_ARGUMENT.withDescription(exception.getMessage());
    }

    @GrpcExceptionHandler({
            CallNotPermittedException.class,
            ExternalServiceException.class
    })
    public StatusRuntimeException externalServiceDenied(Exception exception) {
        Metadata metadata = new Metadata();
        metadata.put(Metadata.Key.of("retry-after", Metadata.ASCII_STRING_MARSHALLER), "10");
        return Status.UNAVAILABLE
                .withDescription("One of the internal services is currently unavailable, please try again later")
                .asRuntimeException(metadata);
    }
}
