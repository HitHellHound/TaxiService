package com.efcon.tg_notification.bot.tg;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.List;
import java.util.function.Predicate;

public final class TgBotKeyboardFactory {
    public static final String NOTIFICATION_ACCEPT_CALLBACK_PREFIX = "accept_ride_";
    public static final String NOTIFICATION_REJECT_CALLBACK_PREFIX = "reject_ride_";


    private TgBotKeyboardFactory() {

    }

    public static ReplyKeyboard notificationButtons(Long rideId) {
        InlineKeyboardButton acceptButton = InlineKeyboardButton
                .builder()
                .text("Accept")
                .callbackData(NOTIFICATION_ACCEPT_CALLBACK_PREFIX + rideId)
                .build();

        InlineKeyboardButton rejectButton = InlineKeyboardButton
                .builder()
                .text("Reject")
                .callbackData(NOTIFICATION_REJECT_CALLBACK_PREFIX + rideId)
                .build();

        return new InlineKeyboardMarkup(List.of(new InlineKeyboardRow(acceptButton, rejectButton)));
    }

    public static Predicate<Update> notificationButtonsCallbackPredicate() {
        return upd -> upd.getCallbackQuery().getData().startsWith(NOTIFICATION_ACCEPT_CALLBACK_PREFIX) ||
                upd.getCallbackQuery().getData().startsWith(NOTIFICATION_REJECT_CALLBACK_PREFIX);
    }
}
