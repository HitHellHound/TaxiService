package com.efcon.passenger.repository;

public final class PassengerRepositoryQueries {
    public static final String SOFT_DELETE_RESTRICTION = " deleted_at IS NULL";

    public static final String GET_PASSENGER_BY_ID = "SELECT * FROM passenger p WHERE id = :id AND" + SOFT_DELETE_RESTRICTION;
    public static final String GET_ALL_PASSENGERS = "SELECT * FROM passenger p WHERE" + SOFT_DELETE_RESTRICTION;
    public static final String GET_PASSENGERS_BY_IDS = "SELECT * FROM passenger WHERE id IN (:ids) AND" + SOFT_DELETE_RESTRICTION;

    public static final String GET_COUNT_OF_ALL_PASSENGERS = "SELECT COUNT(id) FROM passenger WHERE" + SOFT_DELETE_RESTRICTION;

    public static final String INSERT_NEW_PASSENGER = "INSERT INTO passenger(name, email, phone) VALUES(:name, :email, :phone)";

    public static final String UPDATE_PASSENGER_BY_ID = "UPDATE passenger SET name = :name, email = :email, phone =:phone WHERE id = :id AND" + SOFT_DELETE_RESTRICTION;

    public static final String DELETE_PASSENGER_BY_ID = "UPDATE passenger SET deleted_at = NOW() WHERE id = :id";
    public static final String DELETE_ALL_PASSENGERS = "UPDATE passenger SET deleted_at = NOW()";
    public static final String DELETE_PASSENGERS_BY_IDS = "UPDATE passenger SET deleted_at = NOW() WHERE id IN (:ids)";
}
