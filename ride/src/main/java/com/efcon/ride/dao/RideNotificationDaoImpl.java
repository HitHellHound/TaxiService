package com.efcon.ride.dao;

import com.efcon.ride.dto.RideInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.efcon.ride.dao.RideNotificationQueries.*;

@Repository
@RequiredArgsConstructor
public class RideNotificationDaoImpl implements RideNotificationDao {
    private final JdbcClient jdbcClient;
    private final ObjectMapper mapper;

    @Override
    @SneakyThrows
    public void create(RideInfo rideInfo) {
        String rideInfoJson = mapper.writeValueAsString(rideInfo);
        jdbcClient
                .sql(SAVE_NOTIFICATION)
                .param("id", rideInfo.rideId())
                .param("rideInfo", rideInfoJson)
                .param("status",RideNotificationStatus.OPENED.name())
                .update();
    }

    @Override
    @SneakyThrows
    public List<RideInfo> findStaleOpenedNotifications(int seconds) {
        LocalDateTime staleThreshold = LocalDateTime.now().minusSeconds(seconds);
        List<String> rideInfoJsons = jdbcClient.sql(FIND_STALE_OPENED_NOTIFICATIONS)
                .param("staleThreshold", staleThreshold)
                .query(String.class)
                .list();

        List<RideInfo> notifications = new ArrayList<>();
        for (String rideInfoJson :rideInfoJsons) {
            notifications.add(mapper.readValue(rideInfoJson, RideInfo.class));
        }
        return notifications;
    }

    @Override
    public void closeNotificationById(Long id) {
        jdbcClient
                .sql(UPDATE_NOTIFICATION_STATUS_BY_ID)
                .param("id", id)
                .param("status", RideNotificationStatus.CLOSED.name())
                .update();
    }

    @Override
    public void updateLastNotifiedByIds(List<Long> ids) {
        if (!ids.isEmpty()) {
            jdbcClient
                    .sql(UPDATE_LAST_NOTIFIED_AT_BY_IDS)
                    .param("ids", ids)
                    .update();
        }
    }

    public enum RideNotificationStatus {
        OPENED,
        CLOSED
    }
}
