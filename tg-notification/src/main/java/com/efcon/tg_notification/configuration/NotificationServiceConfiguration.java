package com.efcon.tg_notification.configuration;

import com.efcon.tg_notification.wrapper.DefaultLuaScriptWrapper;
import com.efcon.tg_notification.wrapper.LuaScriptWrapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class NotificationServiceConfiguration {
    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(15))
                .disableCachingNullValues();
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericToStringSerializer<>(Object.class));

        return template;
    }

    @Bean
    public LuaScriptWrapper pushRideNotificationToQueuesScript() {
        return new DefaultLuaScriptWrapper("pushRideNotificationToQueues",
                "redis/push-ride-notification-id-to-queues.lua", Void.class);
    }

    @Bean
    public LuaScriptWrapper popNextAndSetActiveRideNotificationScript() {
        return new DefaultLuaScriptWrapper("popNextAndSetActiveRideNotification",
                "redis/pop-next-and-set-active-ride-notification.lua", String.class);
    }

    @Bean
    public LuaScriptWrapper getRideInfoIfNotAcceptedScript() {
        return new DefaultLuaScriptWrapper("getRideInfoIfNotAccepted",
                "redis/get-ride-info-if-not-accepted.lua", String.class);
    }

    @Bean
    public LuaScriptWrapper tryGetRideInfoForAcceptanceScript() {
        return new DefaultLuaScriptWrapper("tryGetRideInfoForAcceptance",
                "redis/try-get-ride-info-for-acceptance.lua", String.class);
    }

    @Bean
    public LuaScriptWrapper setRideAcceptedAndFlushQueueScript() {
        return new DefaultLuaScriptWrapper("setRideAcceptedAndFlushQueue",
                "redis/set-ride-accepted-and-flush-queue.lua", Void.class);
    }
}
