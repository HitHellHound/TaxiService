package com.efcon.tg_notification.bot.tg;

import com.efcon.tg_notification.bot.NotificationBot;
import com.efcon.tg_notification.configuration.TgBotProperties;
import com.efcon.tg_notification.dao.DriverChatDao;
import com.efcon.tg_notification.dto.RideInfo;
import com.efcon.tg_notification.service.DriverChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.abilitybots.api.bot.BaseAbilityBot;
import org.telegram.telegrambots.abilitybots.api.objects.*;
import org.telegram.telegrambots.abilitybots.api.util.AbilityUtils;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.function.BiConsumer;

@Component
public class TgNotificationBot extends AbilityBot implements SpringLongPollingBot, NotificationBot {
    private final String token;
    private final Long creatorId;
    private final DriverChatService driverChatService;

    @Autowired
    public TgNotificationBot(TgBotProperties properties, DriverChatService driverChatService) {
        super(new OkHttpTelegramClient(properties.token()), properties.username());
        this.token = properties.token();
        this.creatorId = properties.creatorId();
        this.driverChatService = driverChatService;
    }

    @Override
    public void sendRideNotification(Long driverId, RideInfo rideInfo) {
        silent.execute(SendMessage.builder()
                .chatId(driverChatService.getChatId(driverId))
                .text(createRideNotificationMessage(rideInfo))
                .replyMarkup(TgNotificationKeyboardFactory.notificationButtons(rideInfo.rideId()))
                .build());
    }

    public Reply processDriverAnswer() {
        BiConsumer<BaseAbilityBot,Update> action = (bot, upd) -> {
            bot.getSilent().execute(EditMessageText
                    .builder()
                    .chatId(AbilityUtils.getChatId(upd))
                    .messageId(upd.getCallbackQuery().getMessage().getMessageId())
                    .text(upd.getCallbackQuery().getData())
                    .build());
        };
        return Reply.of(action, Flag.CALLBACK_QUERY, TgNotificationKeyboardFactory.notificationButtonsCallbackPredicate());
    }

    public Ability testRide() {
        return Ability
                .builder()
                .name("test_ride")
                .input(2)
                .privacy(Privacy.PUBLIC)
                .locality(Locality.ALL)
                .action(ctx -> sendRideNotification(Long.valueOf(ctx.firstArg()), new RideInfo(Long.valueOf(ctx.secondArg()))))
                .build();
    }

    public Ability getChatId() {
        return Ability
                .builder()
                .name("chat_id")
                .input(0)
                .privacy(Privacy.PUBLIC)
                .locality(Locality.ALL)
                .action(ctx -> silent.send(ctx.chatId().toString(), ctx.chatId()))
                .build();
    }

    private String createRideNotificationMessage(RideInfo rideInfo) {
        return new StringBuilder("New Ride #" + rideInfo.rideId()).append('\n')
                .append("Do you want to accept it?")
                .toString();
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
