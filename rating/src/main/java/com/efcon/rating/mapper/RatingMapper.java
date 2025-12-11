package com.efcon.rating.mapper;

import com.efcon.rating.dto.RatingRequest;
import com.efcon.rating.dto.RatingResponse;
import com.efcon.rating.model.Rating;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RatingMapper {
    RatingResponse toResponse(Rating rating);
    List<RatingResponse> toResponseList(List<Rating> ratings);

    @Mapping(source = "score", target = "passengerScore")
    @Mapping(source = "comment", target = "passengerComment")
    void updatePassengerRating(RatingRequest passengerRating, @MappingTarget Rating rating);

    @Mapping(source = "score", target = "driverScore")
    @Mapping(source = "comment", target = "driverComment")
    void updateDriverRating(RatingRequest driverRating, @MappingTarget Rating rating);

    @Mapping(target = "passengerScore", expression = "java(null)")
    @Mapping(target = "passengerComment", expression = "java(null)")
    Rating flushPassengerRating(Rating rating);

    @Mapping(target = "driverScore", expression = "java(null)")
    @Mapping(target = "driverComment", expression = "java(null)")
    Rating flushDriverRating(Rating rating);
}
