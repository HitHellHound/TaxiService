package com.efcon.tg_notification.service;

import com.efcon.tg_notification.dao.DriverChatDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DriverTgIdService implements DriverChatService {
    private final DriverChatDao driverChatIdDao;

    @Override
    public Long getChatId(Long driverId) {
        return driverChatIdDao.getChatId(driverId);
    }

    @Override
    public Long getDriverId(Long chatId) {
        return driverChatIdDao.getDriverId(chatId);
    }
}
