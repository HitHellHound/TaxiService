package com.efcon.tg_notification.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "telegram.bot")
public record TgBotProperties(String token, String username, Long creatorId) {

}
