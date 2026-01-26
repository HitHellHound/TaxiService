package com.efcon.tg_notification.service;

import com.efcon.tg_notification.dao.DriverChatDao;
import com.efcon.tg_notification.exception.DriverChatNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DriverTgIdService implements DriverChatService {
    private final DriverChatDao driverChatIdDao;

    @Override
    @Cacheable(value = "cache::driver", key = "#driverId + '::chat'")
    public Long getChatId(Long driverId) {
        return driverChatIdDao.getChatId(driverId)
                .orElseThrow(() -> new DriverChatNotFoundException("Can't find chat for driver with id " + driverId));
    }

    @Override
    @Cacheable(value = "cache::chat", key = "#chatId + '::driver'")
    public Long getDriverId(Long chatId) {
        return driverChatIdDao.getDriverId(chatId)
                .orElseThrow(() -> new DriverChatNotFoundException("Driver is not registered for this chat"));
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "cache::driver", key = "#driverId + '::chat'"),
            @CacheEvict(value = "cache::chat", key = "#chatId + '::driver'")
    })
    public void registerDriverChat(Long driverId, Long chatId) {
        driverChatIdDao.registerDriverChat(driverId, chatId);
    }
}
