package com.efcon.rating.service;

import com.efcon.rating.AbstractIntegrationTest;
import com.efcon.rating.dto.RatingRequest;
import com.efcon.rating.dto.RatingResponse;
import com.efcon.rating.exception.DocumentNotFoundException;
import com.efcon.rating.exception.EntityNotFoundException;
import com.efcon.rating.exception.IllegalRatingCreationException;
import com.efcon.rating.model.Rating;
import net.datafaker.Faker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Execution(ExecutionMode.SAME_THREAD)
public class RatingServiceIntegrationTest extends AbstractIntegrationTest {
    private static final Long COMPLETED_RIDE_ID = 1L;
    private static final Long ACCEPTED_RIDE_ID = 2L;
    private static final Faker faker = new Faker();

    @Autowired
    private RatingService ratingService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    void shouldCreatePassengerRatingForExistingAndCompletedRide() {
        var ratingRequest = createTestRating();

        var result = ratingService.putPassengerRating(COMPLETED_RIDE_ID, ratingRequest);

        assertThat(result)
                .isNotNull()
                .extracting(RatingResponse::id, RatingResponse::passengerScore, RatingResponse::passengerComment,
                        RatingResponse::driverScore, RatingResponse::driverComment)
                .containsExactly(COMPLETED_RIDE_ID, ratingRequest.score(), ratingRequest.comment(), null, null);

        assertThat(ratingService.getRating(COMPLETED_RIDE_ID))
                .isNotNull()
                .extracting(RatingResponse::id, RatingResponse::passengerScore, RatingResponse::passengerComment,
                        RatingResponse::driverScore, RatingResponse::driverComment)
                .containsExactly(COMPLETED_RIDE_ID, ratingRequest.score(), ratingRequest.comment(), null, null);
    }

    @Test
    void shouldCreateDriverRatingForExistingAndCompletedRide() {
        var ratingRequest = createTestRating();

        var result = ratingService.putDriverRating(COMPLETED_RIDE_ID, ratingRequest);

        assertThat(result)
                .isNotNull()
                .extracting(RatingResponse::id, RatingResponse::passengerScore, RatingResponse::passengerComment,
                        RatingResponse::driverScore, RatingResponse::driverComment)
                .containsExactly(COMPLETED_RIDE_ID, null, null, ratingRequest.score(), ratingRequest.comment());

        assertThat(ratingService.getRating(COMPLETED_RIDE_ID))
                .isNotNull()
                .extracting(RatingResponse::id, RatingResponse::passengerScore, RatingResponse::passengerComment,
                        RatingResponse::driverScore, RatingResponse::driverComment)
                .containsExactly(COMPLETED_RIDE_ID, null, null, ratingRequest.score(), ratingRequest.comment());
    }

    @Test
    void throwIllegalRatingCreationExceptionIfRideIsActive() {
        var ratingRequest = createTestRating();

        assertThatThrownBy(() -> ratingService.putPassengerRating(ACCEPTED_RIDE_ID, ratingRequest))
                .isInstanceOf(IllegalRatingCreationException.class);

        assertThatThrownBy(() -> ratingService.putDriverRating(ACCEPTED_RIDE_ID, ratingRequest))
                .isInstanceOf(IllegalRatingCreationException.class);
    }

    @Test
    void throwEntityNotFoundExceptionIfRideNotExist() {
        var ratingRequest = createTestRating();

        assertThatThrownBy(() -> ratingService.putPassengerRating(Long.MAX_VALUE, ratingRequest))
                .isInstanceOf(EntityNotFoundException.class);

        assertThatThrownBy(() -> ratingService.putDriverRating(Long.MAX_VALUE, ratingRequest))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getThrowDocumentNotFoundException() {
        assertThatThrownBy(() -> ratingService.getRating(Long.MAX_VALUE))
                .isInstanceOf(DocumentNotFoundException.class);
    }

    @Test
    void shouldDeleteRating() {
        var ratingRequest = createTestRating();
        ratingService.putDriverRating(COMPLETED_RIDE_ID, ratingRequest);

        ratingService.deleteRating(COMPLETED_RIDE_ID);

        assertThatThrownBy(() -> ratingService.getRating(COMPLETED_RIDE_ID))
                .isInstanceOf(DocumentNotFoundException.class);
    }

    @Test
    void shouldFlushPassengerRating() {
        var passengerRating = createTestRating();
        var driverRating = createTestRating();

        ratingService.putPassengerRating(COMPLETED_RIDE_ID, passengerRating);
        ratingService.putDriverRating(COMPLETED_RIDE_ID, driverRating);

        ratingService.deletePassengerRating(COMPLETED_RIDE_ID);

        assertThat(ratingService.getRating(COMPLETED_RIDE_ID))
                .isNotNull()
                .extracting(RatingResponse::id, RatingResponse::passengerScore, RatingResponse::passengerComment,
                        RatingResponse::driverScore, RatingResponse::driverComment)
                .containsExactly(COMPLETED_RIDE_ID, null, null, driverRating.score(), driverRating.comment());
    }

    @Test
    void shouldFlushDriverRating() {
        var passengerRating = createTestRating();
        var driverRating = createTestRating();

        ratingService.putPassengerRating(COMPLETED_RIDE_ID, passengerRating);
        ratingService.putDriverRating(COMPLETED_RIDE_ID, driverRating);

        ratingService.deleteDriverRating(COMPLETED_RIDE_ID);

        assertThat(ratingService.getRating(COMPLETED_RIDE_ID))
                .isNotNull()
                .extracting(RatingResponse::id, RatingResponse::passengerScore, RatingResponse::passengerComment,
                        RatingResponse::driverScore, RatingResponse::driverComment)
                .containsExactly(COMPLETED_RIDE_ID, passengerRating.score(), passengerRating.comment(), null, null);
    }

    @Test
    void flushThrowDocumentNotFoundException() {
        assertThatThrownBy(() -> ratingService.deletePassengerRating(Long.MAX_VALUE))
                .isInstanceOf(DocumentNotFoundException.class);
        assertThatThrownBy(() -> ratingService.deleteDriverRating(Long.MAX_VALUE))
                .isInstanceOf(DocumentNotFoundException.class);
    }

    @AfterEach
    void clearCollection() {
        mongoTemplate.remove(new Query(), Rating.class);
    }

    private RatingRequest createTestRating() {
        var random = new Random();
        var score = random.nextInt(1, 6);
        var comment = faker.lorem().paragraph(3);
        System.out.println(comment);
        return new RatingRequest(score, comment);
    }
}
