package com.efcon.rating.repository;

import com.efcon.rating.AbstractIntegrationTest;
import com.efcon.rating.model.Rating;
import net.datafaker.Faker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RatingRepositoryIntegrationTest extends AbstractIntegrationTest {
    private static final Faker faker = new Faker();
    private static final Random random = new Random();

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    void shouldSaveOnlyPassengerScore() {
        var rating = new Rating();
        var id = random.nextLong(1, Long.MAX_VALUE);
        var score = random.nextInt(1, 5);
        rating.setId(id);
        rating.setPassengerScore(score);

        var result = ratingRepository.save(rating);

        assertThat(result)
                .extracting(Rating::getId, Rating::getPassengerScore, Rating::getPassengerComment)
                .containsExactly(id, score, null);

        assertThat(ratingRepository.findById(rating.getId()))
                .isPresent()
                .get()
                .extracting(Rating::getId, Rating::getPassengerScore, Rating::getPassengerComment)
                .containsExactly(id, score, null);
    }

    @Test
    void throwExceptionWhenSavePassengerCommentWithNullScore() {
        var rating = new Rating();
        rating.setId(random.nextLong(1, Long.MAX_VALUE));
        rating.setPassengerComment(faker.yoda().quote());

        assertThatThrownBy(() -> ratingRepository.save(rating))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldSaveOnlyDriverScore() {
        var rating = new Rating();
        var id = random.nextLong(1, Long.MAX_VALUE);
        var score = random.nextInt(1, 5);
        rating.setId(id);
        rating.setDriverScore(score);

        var result = ratingRepository.save(rating);

        assertThat(result)
                .extracting(Rating::getId, Rating::getDriverScore, Rating::getDriverComment)
                .containsExactly(id, score, null);

        assertThat(ratingRepository.findById(rating.getId()))
                .isPresent()
                .get()
                .extracting(Rating::getId, Rating::getDriverScore, Rating::getDriverComment)
                .containsExactly(id, score, null);
    }

    @Test
    void throwExceptionWhenSaveDriverCommentWithNullScore() {
        var rating = new Rating();
        rating.setId(random.nextLong(1, Long.MAX_VALUE));
        rating.setDriverComment(faker.yoda().quote());

        assertThatThrownBy(() -> ratingRepository.save(rating))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldSaveCorrectScore() {
        var rating = new Rating();
        rating.setId(random.nextLong(1, Long.MAX_VALUE));

        rating.setPassengerScore(1);
        rating.setDriverScore(1);
        ratingRepository.save(rating);

        assertThat(ratingRepository.findById(rating.getId()))
                .isPresent()
                .get()
                .extracting(Rating::getPassengerScore, Rating::getDriverScore)
                .containsExactly(1, 1);

        rating.setPassengerScore(5);
        rating.setDriverScore(5);
        ratingRepository.save(rating);

        assertThat(ratingRepository.findById(rating.getId()))
                .isPresent()
                .get()
                .extracting(Rating::getPassengerScore, Rating::getDriverScore)
                .containsExactly(5, 5);
    }

    @Test
    void throwExceptionWhenSaveOutboundScore() {
        var rating = new Rating();
        rating.setId(random.nextLong(1, Long.MAX_VALUE));

        rating.setPassengerScore(0);
        assertThatThrownBy(() -> ratingRepository.save(rating))
                .isInstanceOf(DataIntegrityViolationException.class);

        rating.setPassengerScore(6);
        assertThatThrownBy(() -> ratingRepository.save(rating))
                .isInstanceOf(DataIntegrityViolationException.class);
        rating.setPassengerScore(null);

        rating.setDriverScore(0);
        assertThatThrownBy(() -> ratingRepository.save(rating))
                .isInstanceOf(DataIntegrityViolationException.class);

        rating.setPassengerScore(6);
        assertThatThrownBy(() -> ratingRepository.save(rating))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @AfterEach
    void clearCollection() {
        mongoTemplate.remove(new Query(), Rating.class);
    }
}