package com.efcon.ride.dao;

import com.efcon.ride.dto.RideInfo;

import java.util.List;

public interface RideNotificationDao {
    void create(RideInfo rideInfo);
    List<RideInfo> findStaleOpenedNotifications(int seconds);
    void closeNotificationById(Long id);
    void updateLastNotifiedByIds(List<Long> ids);
}
