package com.efcon.tg_notification.service;

public interface DriverChatService {
    Long getChatId(Long driverId);
    Long getDriverId(Long chatId);
    void registerDriverChat(Long driverId, Long chatId);
}
