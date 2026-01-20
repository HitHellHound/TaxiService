package com.efcon.tg_notification.bot.tg;

import com.efcon.tg_notification.bot.RideNotifier;
import com.efcon.tg_notification.command.AcceptRideNotificationCommand;
import com.efcon.tg_notification.command.RejectRideNotificationCommand;
import com.efcon.tg_notification.command.RideNotificationCommandHandler;
import com.efcon.tg_notification.configuration.TgBotProperties;
import com.efcon.tg_notification.dto.RideInfo;
import com.efcon.tg_notification.service.DriverChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataAccessException;
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
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;

import static com.efcon.tg_notification.bot.tg.TgBotKeyboardFactory.NOTIFICATION_ACCEPT_CALLBACK_PREFIX;
import static com.efcon.tg_notification.bot.tg.TgBotKeyboardFactory.NOTIFICATION_REJECT_CALLBACK_PREFIX;

@Component
public class TgBotRideNotifier extends AbilityBot implements SpringLongPollingBot, RideNotifier {
    private final String token;
    private final Long creatorId;
    private final DriverChatService driverChatService;
    private final RideNotificationCommandHandler commandHandler;
    private boolean isAbilitiesRegistered = false;

    @Autowired
    public TgBotRideNotifier(TgBotProperties properties, DriverChatService driverChatService,
                             RideNotificationCommandHandler commandHandler) {
        super(new OkHttpTelegramClient(properties.token()), properties.username());
        this.token = properties.token();
        this.creatorId = properties.creatorId();
        this.driverChatService = driverChatService;
        this.commandHandler = commandHandler;
    }

    @Override
    public void sendRideNotification(Long driverId, RideInfo rideInfo) {
        Long chatId = driverChatService.getChatId(driverId);
        Optional<Message> message = silent.execute(SendMessage.builder()
                .chatId(chatId)
                .text(createRideNotificationMessage(rideInfo))
                .replyMarkup(TgBotKeyboardFactory.notificationButtons(rideInfo.rideId()))
                .build());
        message.ifPresent(m -> {
            Map<String, Long> activeNotificationMap = getDb().getMap("chat:" + chatId + ":active-notification");
            activeNotificationMap.put("message-id", Long.valueOf(m.getMessageId()));
            activeNotificationMap.put("ride-id", rideInfo.rideId());
        });
    }

    @Override
    public void sendRideAcceptedNotification(Long driverId, RideInfo rideInfo) {
        silent.execute(SendMessage.builder()
                .chatId(driverChatService.getChatId(driverId))
                .text(createRideAcceptedMessage(rideInfo))
                .build());
    }

    @Override
    public void sendRideRejectedNotification(Long driverId, Long rideId) {

    }

    @Override
    public void sendNotificationAcceptanceDecline(Long driverId, Long rideId, String reason) {
        silent.execute(SendMessage.builder()
                .chatId(driverChatService.getChatId(driverId))
                .text("Acceptance for ride #" + rideId + " was declined:\n" + reason)
                .build());
    }

    @Override
    public void sendRideNotificationExpired(Long driverId, Long rideId) {
        Long chatId = driverChatService.getChatId(driverId);
        Map<String, Long> activeNotificationMap = getDb().getMap("chat:" + chatId + ":active-notification");
        if (Objects.equals(activeNotificationMap.get("ride-id"), rideId)
                && activeNotificationMap.containsKey("message-id")) {
            Integer messageId = Math.toIntExact(activeNotificationMap.get("message-id"));
            activeNotificationMap.clear();
            silent.execute(EditMessageText
                    .builder()
                    .chatId(chatId)
                    .messageId(messageId)
                    .text("Notification for ride #" + rideId + " expired.")
                    .build());
        }
    }

    public Reply processDriverAccept() {
        BiConsumer<BaseAbilityBot,Update> action = (bot, upd) -> {
            Long rideId = Long.valueOf(upd.getCallbackQuery().getData().replace(NOTIFICATION_ACCEPT_CALLBACK_PREFIX, ""));

            Map<String, Long> activeNotificationMap = getDb().getMap("chat:" + AbilityUtils.getChatId(upd) + ":active-notification");
            if (Objects.equals(activeNotificationMap.get("ride-id"), rideId)) {
                activeNotificationMap.clear();
            }

            bot.getSilent().execute(EditMessageText
                    .builder()
                    .chatId(AbilityUtils.getChatId(upd))
                    .messageId(upd.getCallbackQuery().getMessage().getMessageId())
                    .text("Process ride #" + rideId + " accept...")
                    .build());

            Long driverId = driverChatService.getDriverId(AbilityUtils.getChatId(upd));
            commandHandler.handle(new AcceptRideNotificationCommand(driverId, rideId));
        };
        return Reply.of(action, Flag.CALLBACK_QUERY, TgBotKeyboardFactory.notificationAcceptButtonCallbackPredicate());
    }

    public Reply processDriverReject() {
        BiConsumer<BaseAbilityBot,Update> action = (bot, upd) -> {
            Long rideId = Long.valueOf(upd.getCallbackQuery().getData().replace(NOTIFICATION_REJECT_CALLBACK_PREFIX, ""));

            Map<String, Long> activeNotificationMap = getDb().getMap("chat:" + AbilityUtils.getChatId(upd) + ":active-notification");
            if (Objects.equals(activeNotificationMap.get("ride-id"), rideId)) {
                activeNotificationMap.clear();
            }

            bot.getSilent().execute(EditMessageText
                    .builder()
                    .chatId(AbilityUtils.getChatId(upd))
                    .messageId(upd.getCallbackQuery().getMessage().getMessageId())
                    .text("Ride #" + rideId + " rejected")
                    .build());

            Long driverId = driverChatService.getDriverId(AbilityUtils.getChatId(upd));
            commandHandler.handle(new RejectRideNotificationCommand(driverId, rideId));
        };
        return Reply.of(action, Flag.CALLBACK_QUERY, TgBotKeyboardFactory.notificationRejectButtonCallbackPredicate());
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

    public Ability registration() {
        return Ability
                .builder()
                .name("register_as")
                .input(1)
                .privacy(Privacy.PUBLIC)
                .locality(Locality.ALL)
                .action(ctx -> {
                    try {
                        driverChatService.registerDriverChat(Long.valueOf(ctx.firstArg()), ctx.chatId());
                        silent.send("Successfully has bind this chat to driver #" + ctx.firstArg(), ctx.chatId());
                    } catch (DataAccessException exception) {
                        silent.send("Can't bind this chat to driver #" + ctx.firstArg(), ctx.chatId());
                    }
                })
                .build();
    }

    private String createRideNotificationMessage(RideInfo rideInfo) {
        return "New Ride #" + rideInfo.rideId() + '\n' +
                createRideInfoBlock(rideInfo) +
                "Do you want to accept it?";
    }

    private String createRideAcceptedMessage(RideInfo rideInfo) {
        return "Ride #" + rideInfo.rideId() + " successfully accepted" + '\n' +
                createRideInfoBlock(rideInfo);
    }

    private String createRideInfoBlock(RideInfo rideInfo) {
        return "INFO:\n" +
                "Date: " + rideInfo.createdAt() + '\n' +
                "Start address: " + rideInfo.startAddress() + '\n' +
                "Destination address: " + rideInfo.destinationAddress() + '\n' +
                "Price: " + rideInfo.price() + '\n';
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
        if (!isAbilitiesRegistered) {
            this.onRegister();
            isAbilitiesRegistered = true;
        }
    }
}
