package com.efcon.ride.dao;

public final class RideNotificationQueries {
    public static final String SAVE_NOTIFICATION = "INSERT INTO ride_notification (id, ride_info, status, created_at, last_notified_at) VALUES (:id, :rideInfo::json, :status::ride_notification_status, NOW(), NOW()) " +
            "ON CONFLICT (id) DO UPDATE SET ride_info = :rideInfo::json, status = :status::ride_notification_status";

    public static final String FIND_STALE_OPENED_NOTIFICATIONS = "SELECT ride_info FROM ride_notification WHERE status = 'OPENED' AND last_notified_at < :staleThreshold";
    public static final String UPDATE_NOTIFICATION_STATUS_BY_ID = "UPDATE ride_notification SET status = :status::ride_notification_status WHERE id = :id";
    public static final String UPDATE_LAST_NOTIFIED_AT_BY_IDS = "UPDATE ride_notification SET last_notified_at = NOW() WHERE id IN (:ids)";
}

