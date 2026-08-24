package com.goBhutan.adminPanel.taxi.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A route published by a driver for an inter-dzongkhag trip.
 * Think of it like a bus route: origin → destination, with a departure time,
 * total seats, rate per km per seat, and a live count of remaining seats.
 */
@Entity
@Table(name = "tbl_taxi_inter_route")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InterRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** FK to driver (your existing User/Driver entity) */
    @Column(name = "driver_id", nullable = false)
    private String driverId;

    @Column(name = "origin_dzongkhag", nullable = false)
    private String originDzongkhag;

    @Column(name = "origin_address", nullable = false)
    private String originAddress;

    @Column(name = "destination_dzongkhag", nullable = false)
    private String destinationDzongkhag;

    @Column(name = "destination_address", nullable = false)
    private String destinationAddress;

    // Add this:
    @OneToMany(mappedBy = "route",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER)
    @OrderBy("stopSequence ASC")
    @Builder.Default
    private List<RouteStop> stops = new ArrayList<>();

    @Column(name = "route_distance_km", nullable = false, precision = 8, scale = 2)
    private BigDecimal routeDistanceKm;

    /** Driver-set rate per km per seat (Nu) */
    @Column(name = "rate_per_km_per_seat", nullable = false, precision = 8, scale = 2)
    private BigDecimal ratePerKmPerSeat;

    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats;

    /** Decremented on each confirmed booking, incremented on cancellation */
    @Column(name = "available_seats", nullable = false)
    private Integer availableSeats;

    @Column(name = "departure_time", nullable = false)
    private LocalDateTime departureTime;

    /**
     * Platform-applied surge multiplier (e.g. 1.0 = normal, 1.5 = peak).
     * Stored here so it's locked to the route at creation time.
     */
    @Column(name = "surge_multiplier", nullable = false, precision = 4, scale = 2)
    private BigDecimal surgeMultiplier;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.surgeMultiplier == null) this.surgeMultiplier = BigDecimal.ONE;
        if (this.isActive == null) this.isActive = true;
    }
}
