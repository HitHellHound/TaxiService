package com.efcon.passenger.repository;

import com.efcon.passenger.exception.EntityUpdateException;
import com.efcon.passenger.model.Passenger;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.efcon.passenger.repository.PassengerRepositoryQueries.*;

@Repository
@RequiredArgsConstructor
public class PassengerRepositoryImpl implements PassengerRepository {
    private final JdbcClient jdbcClient;
    private final JdbcTemplate jdbcTemplate;

    private static final int BATCH_SIZE = 100;

    @Override
    @Transactional
    public <S extends Passenger> S save(S passenger) {
        if (passenger.getId() == null) {
            return insertNewPassenger(passenger);
        }
        return updatePassenger(passenger);
    }

    private <S extends Passenger> S insertNewPassenger(S passenger) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcClient
                .sql(INSERT_NEW_PASSENGER)
                .paramSource(passenger)
                .update(keyHolder, "id");
        passenger.setId(keyHolder.getKeyAs(Long.class));
        return passenger;
    }

    private <S extends Passenger> S updatePassenger(S passenger) {
        int updates = jdbcClient
                .sql(UPDATE_PASSENGER_BY_ID)
                .paramSource(passenger)
                .update();
        if (updates != 1) {
            throw new EntityUpdateException("Can't update passenger with id " + passenger.getId());
        }
        return passenger;
    }

    @Override
    @Transactional
    public <S extends Passenger> List<S> saveAll(Iterable<S> passengers) {
        Map<Boolean, List<S>> isPassengerNewLists = StreamSupport
                .stream(passengers.spliterator(), true)
                .collect(Collectors.partitioningBy(passenger -> passenger.getId() == null));
        return Stream.concat(
                        batchInsertNewPassenger(isPassengerNewLists.get(true)).stream(),
                        batchUpdatePassenger(isPassengerNewLists.get(false)).stream())
                .toList();
    }

    private <S extends Passenger> List<S> batchInsertNewPassenger(List<S> passengers) {
        return passengers.stream().map(this::insertNewPassenger).toList();
    }

    private <S extends Passenger> List<S> batchUpdatePassenger(List<S> passengers) {
        int[][] updates = jdbcTemplate.batchUpdate("UPDATE passenger SET name = ?, email = ?, phone = ? WHERE id = ?",
                passengers,
                BATCH_SIZE,
                (PreparedStatement ps, S p) -> {
                    ps.setString(1, p.getName());
                    ps.setString(2, p.getEmail());
                    ps.setString(3, p.getPhone());
                    ps.setLong(3, p.getId());
                });
        for (int[] update : updates) {
            for (int i : update) {
                if (i != 1) {
                    throw new EntityUpdateException("Something goes wrong during passengers update");
                }
            }
        }
        return passengers;
    }

    @Override
    public Optional<Passenger> findById(Long id) {
        return jdbcClient
                .sql(GET_PASSENGER_BY_ID)
                .param("id", id)
                .query(Passenger.class)
                .optional();
    }

    @Override
    public boolean existsById(Long id) {
        return findById(id).isPresent();
    }

    @Override
    public List<Passenger> findAll() {
        return jdbcClient
                .sql(GET_ALL_PASSENGERS)
                .query(Passenger.class)
                .list();
    }

    @Override
    public List<Passenger> findAllById(Iterable<Long> ids) {
        return jdbcClient
                .sql(GET_PASSENGERS_BY_IDS)
                .param("ids", ids)
                .query(Passenger.class)
                .list();
    }

    @Override
    public long count() {
        return jdbcClient
                .sql(GET_COUNT_OF_ALL_PASSENGERS)
                .query(Long.class)
                .single();
    }

    @Override
    public void deleteById(Long id) {
        jdbcClient
                .sql(DELETE_PASSENGER_BY_ID)
                .param("id", id)
                .update();
    }

    @Override
    public void delete(Passenger passenger) {
        deleteById(passenger.getId());
    }

    @Override
    public void deleteAllById(Iterable<? extends Long> ids) {
        jdbcClient
                .sql(DELETE_PASSENGERS_BY_IDS)
                .param("ids", ids.iterator().hasNext() ? ids : List.of())
                .update();
    }

    @Override
    public void deleteAll(Iterable<? extends Passenger> entities) {
        deleteAllById(StreamSupport
                .stream(entities.spliterator(), true)
                .map(Passenger::getId)
                .toList()
        );
    }

    @Override
    public void deleteAll() {
        jdbcClient
                .sql(DELETE_ALL_PASSENGERS)
                .update();
    }
}
