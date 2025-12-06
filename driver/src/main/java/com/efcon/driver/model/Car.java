package com.efcon.driver.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "car")
@SQLDelete(sql = "UPDATE car SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
public class Car {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String color;

    @NotBlank
    @Column(nullable = false)
    private String brand;

    @Pattern(
            regexp = "^\\d{4}[A-Z]{2}-[1-8]$",
            message = "must match Belarus format XXXXYY-Z"
    )
    @Column(nullable = false)
    private String number;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public Car(String color, String brand, String number) {
        this.color = color;
        this.brand = brand;
        this.number = number;
    }
}
