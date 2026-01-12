package com.efcon.tg_notification.dao;

import com.efcon.tg_notification.dto.RideInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

import static com.efcon.tg_notification.service.RedisKeyTemplates.*;

@Repository
@RequiredArgsConstructor
public class RideNotificationRedisDao implements RideNotificationDao {
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void pushRideNotificationQueue(Long driverId, Long rideId) {
        redisTemplate.opsForList().rightPush(String.format(DRIVER_NOTIFICATION_QUEUE_TEMPLATE, driverId),
                rideId);
    }

    @Override
    public Optional<RideInfo> popNextAndSetActiveRideNotification(Long driverId) {
        Long nextNotificationId = (Long) redisTemplate.opsForList().leftPop(String.format(DRIVER_NOTIFICATION_QUEUE_TEMPLATE, driverId));
        while (nextNotificationId != null) {
            RideInfo rideInfo = (RideInfo) redisTemplate.opsForValue().get(String.format(RIDE_INFO_TEMPLATE, nextNotificationId));
            if (rideInfo != null && !redisTemplate.hasKey(String.format(RIDE_ACCEPTED_TEMPLATE, nextNotificationId))) {
                redisTemplate.opsForValue()
                        .set(String.format(DRIVER_ACTIVE_RIDE_NOTIFICATION_TEMPLATE, driverId), nextNotificationId);
                return Optional.of(rideInfo);
            } else {
                nextNotificationId = (Long) redisTemplate.opsForList().leftPop(String.format(DRIVER_NOTIFICATION_QUEUE_TEMPLATE, driverId));
            }
        }
        redisTemplate.delete(String.format(DRIVER_ACTIVE_RIDE_NOTIFICATION_TEMPLATE, driverId));
        return Optional.empty();
    }

    @Override
    public void dropRideNotificationQueue(Long driverId) {
        redisTemplate.delete(String.format(DRIVER_NOTIFICATION_QUEUE_TEMPLATE, driverId));
    }

    @Override
    public Optional<Long> getActiveRideNotificationId(Long driverId) {
        return Optional.ofNullable((Long) redisTemplate.opsForValue()
                .get(String.format(DRIVER_ACTIVE_RIDE_NOTIFICATION_TEMPLATE, driverId)));
    }

    @Override
    public boolean hasActiveRideNotification(Long driverId) {
        return redisTemplate.hasKey(String.format(DRIVER_ACTIVE_RIDE_NOTIFICATION_TEMPLATE, driverId));
    }

    @Override
    public void removeActiveRideNotification(Long driverId) {
        redisTemplate.delete(String.format(DRIVER_ACTIVE_RIDE_NOTIFICATION_TEMPLATE, driverId));
    }

    @Override
    public void setRideAccepted(Long rideId) {
        redisTemplate.opsForValue().set(String.format(RIDE_ACCEPTED_TEMPLATE, rideId), "true",
                Duration.ofMinutes(15));
    }

    @Override
    public boolean isRideAccepted(Long rideId) {
        return redisTemplate.hasKey(String.format(RIDE_ACCEPTED_TEMPLATE, rideId));
    }

    @Override
    public void addRideInfo(Long rideId, RideInfo rideInfo) {
        redisTemplate.opsForValue().set(String.format(RIDE_INFO_TEMPLATE, rideId), rideInfo, Duration.ofMinutes(5));
    }

    @Override
    public Optional<RideInfo> getRideInfo(Long rideId) {
        return Optional.ofNullable((RideInfo) redisTemplate.opsForValue()
                .get(String.format(RIDE_INFO_TEMPLATE, rideId)));
    }

    @Override
    public void removeRideInfo(Long rideId) {
        redisTemplate.delete(String.format(RIDE_INFO_TEMPLATE, rideId));
    }
}
