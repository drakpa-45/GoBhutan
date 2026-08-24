package com.goBhutan.adminPanel.taxi.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_taxi_rating",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_booking_rating",
                columnNames = "booking_id"))   // one rating per booking
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TaxiRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** One rating per completed booking */
    @Column(name = "booking_id", nullable = false, unique = true)
    private Long bookingId;

    /** The passenger who rated */
    @Column(name = "passenger_id", nullable = false)
    private String passengerId;

    /** The driver being rated */
    @Column(name = "driver_id", nullable = false)
    private String driverId;

    /** 1–5 stars */
    @Column(name = "rating", nullable = false)
    private Integer rating;

    /** Optional comment */
    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}