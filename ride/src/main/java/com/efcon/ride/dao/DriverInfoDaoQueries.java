package com.efcon.ride.dao;

public final class DriverInfoDaoQueries {
    public static final String SOFT_DELETE_RESTRICTION = " deleted_at IS NULL";

    public static final String GET_DRIVER_BY_ID = "SELECT * FROM driver_info di WHERE id = :id AND" + SOFT_DELETE_RESTRICTION;
    public static final String GET_SOME_FREE_DRIVER_ON_CAR_IDS = "SELECT id FROM driver_info di " +
            "WHERE car_id IS NOT NULL AND status = 'FREE' AND deleted_at IS NULL LIMIT :maxNumber";

    public static final String SAVE_DRIVER = "INSERT INTO driver_info (id, car_id, status) VALUES (:id, :carId, :statusName::driver_status) " +
            "ON CONFLICT (id) DO UPDATE SET car_id = :carId, status = :statusName::driver_status";

    public static final String ATTACH_CAR_TO_DRIVER_BY_ID = "UPDATE driver_info SET car_id = :carId WHERE id = :id AND" + SOFT_DELETE_RESTRICTION;
    public static final String DETACH_CAR_FROM_DRIVER_BY_ID = "UPDATE driver_info SET car_id = NULL WHERE id = :id AND" + SOFT_DELETE_RESTRICTION;
    public static final String UPDATE_DRIVER_STATUS_BY_ID = "UPDATE driver_info SET status = :status::driver_status WHERE id = :id AND" + SOFT_DELETE_RESTRICTION;

    public static final String DELETE_DRIVER_BY_ID = "UPDATE driver_info SET deleted_at = NOW() WHERE id = :id";
}
