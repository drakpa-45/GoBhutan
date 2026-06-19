package com.goBhutan.adminPanel.taxi.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Append-only log of driver positions during an active trip.
 * One row every 30 seconds from trip_started_at to trip_ended_at.
 *
 * Used for:
 *  - Trip path replay (passenger can see route taken)
 *  - Distance verification (cross-check against booked distance)
 *  - Dispute evidence
 *  - Future: heatmap analytics for surge pricing decisions
 */
@Entity
@Table(name = "tbl_taxi_trip_location_history",
       indexes = {
           @Index(name = "idx_tlh_booking",   columnList = "booking_id, recorded_at"),
           @Index(name = "idx_tlh_driver",    columnList = "driver_id,  recorded_at")
       })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TripLocationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Column(name = "driver_id", nullable = false)
    private Long driverId;

    @Column(name = "latitude", nullable = false, precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(name = "longitude", nullable = false, precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "bearing", precision = 6, scale = 2)
    private BigDecimal bearing;

    @Column(name = "speed_kmh", precision = 6, scale = 2)
    private BigDecimal speedKmh;

    /**
     * Cumulative distance driven so far in this trip (km).
     * Computed incrementally using Haversine from the previous point.
     */
    @Column(name = "cumulative_distance_km", precision = 8, scale = 3)
    private BigDecimal cumulativeDistanceKm;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @PrePersist
    public void onCreate() {
        if (this.recordedAt == null) this.recordedAt = LocalDateTime.now();
    }
}
