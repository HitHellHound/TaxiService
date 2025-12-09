package com.efcon.passenger.mapper;

import org.mapstruct.MappingTarget;

import java.util.List;

public interface CRUDMapper<E, T, K> {
    K toResponseDto(E entity);
    List<K> toResponseDtoList(List<E> entities);
    E fromRequestDto(T dto);
    void updateEntityFromDto(T dto, @MappingTarget E entity);
}
