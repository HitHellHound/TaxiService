package com.efcon.tg_notification.dao;

import java.util.Optional;

public interface DriverChatDao {
    Optional<Long> getChatId(Long driverId);
    Optional<Long> getDriverId(Long chatId);
    void registerDriverChat(Long driverId, Long chatId);
}
