package com.efcon.tg_notification.command;

public interface RideNotificationCommandHandler {
    void handle(AcceptRideNotificationCommand command);
    void handle(RejectRideNotificationCommand command);
}
