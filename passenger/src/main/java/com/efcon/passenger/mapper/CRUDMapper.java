package com.efcon.passenger.mapper;

import org.mapstruct.MappingTarget;

import java.util.List;

public interface CRUDMapper<E, T, K> {
    K toResponse(E entity);
    List<K> toResponseList(List<E> entities);
    E fromRequest(T request);
    void updateEntityFromRequest(T request, @MappingTarget E entity);
}
