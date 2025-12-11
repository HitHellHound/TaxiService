package com.efcon.driver.model;

import jakarta.persistence.*;
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

    @Column(nullable = false)
    private String color;

    @Column(nullable = false)
    private String brand;

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
