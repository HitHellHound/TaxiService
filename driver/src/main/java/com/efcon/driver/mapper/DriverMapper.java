package com.efcon.driver.mapper;

import com.efcon.driver.dto.DriverRequestDTO;
import com.efcon.driver.dto.DriverResponseDTO;
import com.efcon.driver.model.Driver;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {CarMapper.class})
public interface DriverMapper extends CRUDMapper<Driver, DriverRequestDTO, DriverResponseDTO> {
    @Override
    DriverResponseDTO toResponseDto(Driver entity);

    @Override
    List<DriverResponseDTO> toResponseDtoList(List<Driver> entities);

    @Override
    Driver fromRequestDto(DriverRequestDTO dto);

    @Override
    void updateEntityFromDto(DriverRequestDTO dto, @MappingTarget Driver entity);
}
