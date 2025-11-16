package com.goBhutan.adminPanel.busAdmin.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;

@Entity
@Table(name = "tbl_bs_bus_route_map")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BusRouteMap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bus_id", nullable = false)
    @JsonIgnore
    private Bus bus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    @JsonIgnore
    private Route route;

    @NotNull
    @Column(name = "departure_time", nullable = false)
    private LocalTime departureTime;

    @Column(name = "custom_fare", precision = 10, scale = 2)
    private BigDecimal customFare;

    @Column(name = "estimated_duration", nullable = true)
    private Integer estimatedDuration; // override route duration

    @Column(name = "active", nullable = false)
    private Boolean active = true;
}
