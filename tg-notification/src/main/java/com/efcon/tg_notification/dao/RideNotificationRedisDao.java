package com.efcon.tg_notification.dao;

import com.efcon.tg_notification.dto.RideInfo;
import com.efcon.tg_notification.wrapper.LuaScriptWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.*;

import static com.efcon.tg_notification.dao.RedisKeyTemplates.*;

@Repository
@RequiredArgsConstructor
public class RideNotificationRedisDao implements RideNotificationDao, LuaScriptAware {
    @Value("${ride-notifications.driver.queue.max-size}")
    private Integer queueMaxSize;
    @Value("${ride-notifications.ride.info.ttl-minutes}")
    private Integer rideInfoTtlInMinutes;
    @Value("${ride-notifications.ride.accepted-flag.ttl-minutes}")
    private Integer rideAcceptedTtlFlagInMinutes;

    private final ObjectMapper jsonMapper;
    private final RedisTemplate<String, String> redisTemplate;
    private final Map<String, RedisScript<?>> luaScripts = new HashMap<>();

    @Override
    public void pushRideNotificationQueue(Long driverId, Long rideId) {
        redisTemplate.opsForZSet().addIfAbsent(String.format(DRIVER_NOTIFICATION_QUEUE_TEMPLATE, driverId),
                rideId.toString(), (double) System.currentTimeMillis());
    }

    @Override
    public void pushRideNotificationQueues(Set<Long> driverIds, Long rideId) {
        redisTemplate.execute(luaScripts.get("pushRideNotificationToQueues"),
                driverIds.stream().map(id -> String.format(DRIVER_NOTIFICATION_QUEUE_TEMPLATE, id)).toList(),
                rideId, queueMaxSize);
    }

    @Override
    public Optional<RideInfo> popNextAndSetActiveRideNotification(Long driverId, boolean onlyIfNoneActive) {
        String rideInfoJson = (String) redisTemplate.execute(luaScripts.get("popNextAndSetActiveRideNotification"),
                List.of(
                        String.format(DRIVER_NOTIFICATION_QUEUE_TEMPLATE, driverId),
                        String.format(DRIVER_ACTIVE_RIDE_NOTIFICATION_TEMPLATE, driverId)
                ),
                RIDE_INFO_TEMPLATE, RIDE_ACCEPTED_TEMPLATE, onlyIfNoneActive ? "1" : "0");

        return rideInfoJson == null ? Optional.empty() :
                Optional.of(jsonMapper.readValue(rideInfoJson, RideInfo.class));
    }

    @Override
    public Optional<Long> getActiveRideNotificationId(Long driverId) {
        String idString = redisTemplate.opsForValue()
                .get(String.format(DRIVER_ACTIVE_RIDE_NOTIFICATION_TEMPLATE, driverId));
        return idString == null ? Optional.empty() : Optional.of(Long.valueOf(idString));
    }

    @Override
    public boolean hasActiveRideNotification(Long driverId) {
        return redisTemplate.hasKey(String.format(DRIVER_ACTIVE_RIDE_NOTIFICATION_TEMPLATE, driverId));
    }

    @Override
    public void setRideAcceptedAndFlushQueue(Long rideId, Long driverId) {
        redisTemplate.execute(luaScripts.get("setRideAcceptedAndFlushQueue"),
                List.of(String.format(RIDE_ACCEPTED_TEMPLATE, rideId),
                        String.format(DRIVER_NOTIFICATION_QUEUE_TEMPLATE, driverId),
                        String.format(DRIVER_ACTIVE_RIDE_NOTIFICATION_TEMPLATE, driverId)),
                rideAcceptedTtlFlagInMinutes);
    }

    @Override
    public void addRideInfo(Long rideId, RideInfo rideInfo) {
        redisTemplate.opsForValue().set(String.format(RIDE_INFO_TEMPLATE, rideId),
                jsonMapper.writeValueAsString(rideInfo),
                Duration.ofMinutes(rideInfoTtlInMinutes));
    }

    @Override
    public Optional<RideInfo> getRideInfoIfNotAccepted(Long rideId) {
        String rideInfoJson = (String) redisTemplate.execute(luaScripts.get("getRideInfoIfNotAccepted"),
                List.of(String.format(RIDE_INFO_TEMPLATE, rideId),
                        String.format(RIDE_ACCEPTED_TEMPLATE, rideId)));
        return rideInfoJson == null ? Optional.empty() :
                Optional.of(jsonMapper.readValue(rideInfoJson, RideInfo.class));
    }

    @Override
    @Autowired
    public void registerLuaScripts(List<LuaScriptWrapper> scripts) {
        scripts.forEach(script -> luaScripts.put(script.getName(), script.getScript()));
    }
}
