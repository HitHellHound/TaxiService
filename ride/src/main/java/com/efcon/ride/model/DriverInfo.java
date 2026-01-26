package com.efcon.ride.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DriverInfo {
    private Long id;
    private Long carId;
    private DriverStatus status;

    public String getStatusName() {
        return status.name();
    }
}
