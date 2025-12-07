package com.efcon.ride.model;

import com.efcon.ride.validation.ActiveRideDriverNotNull;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ride")
@SQLDelete(sql = "UPDATE ride SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@ActiveRideDriverNotNull
public class Ride {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "driver_id")
    private Long driverId;

    @NotNull
    @Column(name = "passenger_id", nullable = false)
    private Long passengerId;

    @NotBlank
    @Size(max = 255)
    @Column(name = "start_address", nullable = false)
    private String startAddress;

    @NotBlank
    @Size(max = 255)
    @Column(name = "destination_address", nullable = false)
    private String destinationAddress;

    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(columnDefinition = "ride_status", nullable = false)
    private RideStatus status;

    @NotNull(message = "Creation time is not set")
    @PastOrPresent(message = "Creation time cannot be in the future")
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @NotNull
    @Positive
    @Digits(integer = 10, fraction = 2)
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
