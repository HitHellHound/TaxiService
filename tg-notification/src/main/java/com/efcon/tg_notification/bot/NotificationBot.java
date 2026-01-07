package com.efcon.tg_notification.bot;

import com.efcon.tg_notification.configuration.TgBotProperties;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;

@Component
public class NotificationBot extends AbilityBot implements SpringLongPollingBot {
    private final String token;
    private final Long creatorId;

    public NotificationBot(TgBotProperties properties) {
        super(new OkHttpTelegramClient(properties.token()), properties.username());
        this.token = properties.token();
        this.creatorId = properties.creatorId();
    }

    @Override
    public long creatorId() {
        return creatorId;
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @EventListener({ContextRefreshedEvent.class})
    private void initAbilities() {
        this.onRegister();
    }
}
