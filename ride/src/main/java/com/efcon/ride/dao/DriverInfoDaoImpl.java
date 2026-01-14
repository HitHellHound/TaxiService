package com.efcon.ride.dao;

import com.efcon.ride.model.DriverInfo;
import com.efcon.ride.model.DriverStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static com.efcon.ride.dao.DriverInfoDaoQueries.*;

@Repository
@RequiredArgsConstructor
public class DriverInfoDaoImpl implements DriverInfoDao {
    private final JdbcClient jdbcClient;

    @Override
    public Optional<DriverInfo> get(Long id) {
        return jdbcClient.sql(GET_DRIVER_BY_ID)
                .param("id", id)
                .query(DriverInfo.class)
                .optional();
    }

    @Override
    public void save(DriverInfo driverInfo) {
        jdbcClient
                .sql(SAVE_DRIVER)
                .paramSource(driverInfo)
                .update();
    }

    @Override
    public int attachCar(Long driverId, Long carId) {
        return jdbcClient
                .sql(ATTACH_CAR_TO_DRIVER_BY_ID)
                .param("id", driverId)
                .param("carId", carId)
                .update();
    }

    @Override
    public int detachCar(Long driverId) {
        return jdbcClient
                .sql(DETACH_CAR_FROM_DRIVER_BY_ID)
                .param("id", driverId)
                .update();
    }

    @Override
    public int changeDriverStatus(Long id, DriverStatus status) {
        return jdbcClient
                .sql(UPDATE_DRIVER_STATUS_BY_ID)
                .param("id", id)
                .param("status", status.name())
                .update();
    }

    @Override
    public void delete(Long id) {
        jdbcClient
                .sql(DELETE_DRIVER_BY_ID)
                .param("id", id)
                .update();
    }
}
