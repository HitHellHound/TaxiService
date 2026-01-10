package com.efcon.tg_notification.service;

import com.efcon.tg_notification.dao.DriverChatDao;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DriverTgIdService implements DriverChatService {
    private final DriverChatDao driverChatIdDao;

    @Override
    @Cacheable(value = "cache::driver", key = "#driverId + '::chat'")
    public Long getChatId(Long driverId) {
        return driverChatIdDao.getChatId(driverId);
    }

    @Override
    @Cacheable(value = "cache::chat", key = "#chatId + '::driver'")
    public Long getDriverId(Long chatId) {
        return driverChatIdDao.getDriverId(chatId);
    }
}
