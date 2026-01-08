package com.efcon.tg_notification.dao;

public interface DriverChatDao {
    Long getChatId(Long driverId);
    Long getDriverId(Long chatId);
}
