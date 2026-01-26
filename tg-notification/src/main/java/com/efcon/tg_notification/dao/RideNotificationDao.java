package com.efcon.tg_notification.dao;

import com.efcon.tg_notification.dto.ExpiredNotificationTuple;
import com.efcon.tg_notification.dto.RideInfo;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface RideNotificationDao {
    void pushRideNotificationQueue(Long driverId, Long rideId);
    void pushRideNotificationQueues(Set<Long> driverId, Long rideId);
    Optional<RideInfo> popNextAndSetActiveRideNotification(Long driverId, boolean onlyIfNoneActive);

    Optional<RideInfo> tryGetRideInfoForAcceptance(Long rideId, Long driverId);
    void setRideAcceptedAndFlushQueue(Long rideId, Long driverId);

    void addRideInfo(Long rideId, RideInfo rideInfo);
    Optional<RideInfo> getRideInfoIfNotAccepted(Long rideId);

    List<ExpiredNotificationTuple> getExpiredNotifications();
    boolean tryToExpireActiveNotification(Long driverId, Long rideId);
}
