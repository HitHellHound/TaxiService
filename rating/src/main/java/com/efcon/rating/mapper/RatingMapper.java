package com.efcon.rating.mapper;

import com.efcon.rating.dto.RatingRequestDTO;
import com.efcon.rating.dto.RatingResponseDTO;
import com.efcon.rating.model.Rating;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RatingMapper {
    RatingResponseDTO toResponseDto(Rating rating);
    List<RatingResponseDTO> toResponseDtoList(List<Rating> ratings);

    @Mapping(source = "score", target = "passengerScore")
    @Mapping(source = "comment", target = "passengerComment")
    void updatePassengerRating(RatingRequestDTO passengerRating, @MappingTarget Rating rating);

    @Mapping(source = "score", target = "driverScore")
    @Mapping(source = "comment", target = "driverComment")
    void updateDriverRating(RatingRequestDTO driverRating, @MappingTarget Rating rating);

    @Mapping(target = "passengerScore", expression = "java(null)")
    @Mapping(target = "passengerComment", expression = "java(null)")
    Rating flushPassengerRating(Rating rating);

    @Mapping(target = "driverScore", expression = "java(null)")
    @Mapping(target = "driverComment", expression = "java(null)")
    Rating flushDriverRating(Rating rating);
}
