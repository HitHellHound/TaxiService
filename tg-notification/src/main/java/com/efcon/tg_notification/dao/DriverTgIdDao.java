package com.efcon.tg_notification.dao;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class DriverTgIdDao implements DriverChatDao {
    private final JdbcClient jdbcClient;
    private static final String GET_CHAT_ID_BY_DRIVER_ID = "SELECT tg_id FROM driver_tg WHERE driver_id = :driver_id";
    private static final String GET_DRIVER_ID_BY_CHAT_ID = "SELECT driver_id FROM driver_tg WHERE tg_id = :tg_id";
    private static final String REGISTER_DRIVER_CHAT = "INSERT INTO driver_tg (driver_id, tg_id) VALUES (:driver_id, :tg_id)";

    public DriverTgIdDao(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Optional<Long> getChatId(Long driverId) {
        return jdbcClient
                .sql(GET_CHAT_ID_BY_DRIVER_ID)
                .param("driver_id",driverId)
                .query(Long.class)
                .optional();
    }

    @Override
    public Optional<Long> getDriverId(Long chatId) {
        return jdbcClient
                .sql(GET_DRIVER_ID_BY_CHAT_ID)
                .param("tg_id", chatId)
                .query(Long.class)
                .optional();
    }

    @Override
    public void registerDriverChat(Long driverId, Long chatId) {
        jdbcClient
                .sql(REGISTER_DRIVER_CHAT)
                .param("driver_id", driverId)
                .param("tg_id", chatId)
                .update();
    }
}
