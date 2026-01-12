package com.efcon.tg_notification.service;

public final class RedisKeyTemplates {
    public static final String DRIVER_NOTIFICATION_QUEUE_TEMPLATE = "driver:%d:notification-queue";
    public static final String RIDE_INFO_TEMPLATE = "ride:%d:info";
    public static final String DRIVER_ACTIVE_RIDE_NOTIFICATION_TEMPLATE = "driver:%d:active-notification";
    public static final String RIDE_ACCEPTED_TEMPLATE = "ride:%d:accepted";
}
