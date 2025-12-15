package com.efcon.rating.repository;

import com.efcon.rating.model.Rating;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RatingRepository extends MongoRepository<Rating, Long> {
}
