package com.efcon.driver.mapper;

import com.efcon.driver.dto.CarRequest;
import com.efcon.driver.dto.CarResponse;
import com.efcon.driver.model.Car;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CarMapper extends CRUDMapper<Car, CarRequest, CarResponse> {
}
