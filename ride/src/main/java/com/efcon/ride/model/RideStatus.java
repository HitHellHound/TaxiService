package com.efcon.ride.model;

import java.util.Map;
import java.util.Set;

public enum RideStatus {
    CREATED,
    ACCEPTED,
    DRIVING_TO_PASSENGER,
    DRIVING_TO_DESTINATION,
    COMPLETED,
    CANCELED;

    private static final Map<RideStatus, Set<RideStatus>> ALLOWED_TRANSITIONS = Map.ofEntries(
            Map.entry(CREATED, Set.of(ACCEPTED, CANCELED)),
            Map.entry(ACCEPTED, Set.of(DRIVING_TO_PASSENGER, CANCELED)),
            Map.entry(DRIVING_TO_PASSENGER, Set.of(DRIVING_TO_DESTINATION, CANCELED)),
            Map.entry(DRIVING_TO_DESTINATION, Set.of(COMPLETED)),
            Map.entry(COMPLETED, Set.of()),
            Map.entry(CANCELED, Set.of())
    );

    public static boolean isTransitionAllowed(RideStatus currentStatus, RideStatus newStatus) {
        return ALLOWED_TRANSITIONS.get(currentStatus).contains(newStatus);
    }
}
