package com.efcon.tg_notification.dto;

import java.io.Serializable;
import java.util.Set;

public record RideNotification(RideInfo rideInfo, Set<Long> driverIds) implements Serializable {
}
