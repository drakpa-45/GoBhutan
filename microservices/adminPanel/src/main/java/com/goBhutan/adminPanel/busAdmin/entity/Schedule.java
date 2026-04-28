package com.goBhutan.adminPanel.busAdmin.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@Table(
        name = "tbl_bs_schedules",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_bs_schedule_bus_route_departure",
                columnNames = {"bus_id", "route_id", "departure_time"}
        )
)
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Departure time is required")
    @Column(name = "departure_time", nullable = false)
    private LocalDateTime departureTime;

    @NotNull(message = "Arrival time is required")
    @Column(name = "arrival_time", nullable = false)
    private LocalDateTime arrivalTime;

    @Column(name = "available_seats")
    private Integer availableSeats;

    @Column(name = "base_fare", precision = 10, scale = 2)
    private BigDecimal baseFare;

    @Column(name = "app_charges", precision = 10, scale = 2)
    private BigDecimal appCharges;

    @Column(name = "final_fare", precision = 10, scale = 2)
    private BigDecimal finalFare;

    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bus_id", nullable = false)
    @JsonIgnore
    private Bus bus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    @JsonIgnore
    private BusRoute route;

    @Transient
    public BigDecimal getBaseFare() {
        return baseFare != null ? baseFare : route != null ? route.getBaseFare() : null;
    }

    @Transient
    public BigDecimal getAppCharges() {
        return appCharges != null ? appCharges : route != null ? route.getAppCharges() : BigDecimal.ZERO;
    }

    @Transient
    public BigDecimal getFinalFare() {
        return finalFare != null ? finalFare : route != null ? route.getFinalFare() : null;
    }

    @Transient
    public boolean hasFareSnapshot() {
        return baseFare != null && appCharges != null && finalFare != null;
    }

//    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    @JsonIgnore
//    private List<Bookings> bookings = new ArrayList<>();

}
