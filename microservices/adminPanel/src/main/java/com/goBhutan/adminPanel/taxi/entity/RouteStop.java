package com.goBhutan.adminPanel.taxi.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

/**
 * Each stop along an inter-dzongkhag route.
 * Passengers can board at any stop and alight at any later stop.
 * Fare is calculated based on distance between their boarding and alighting stop.
 */
@Entity
@Table(name = "tbl_taxi_route_stop")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RouteStop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private InterRoute route;

    /** 0 = origin, last = destination, everything in between = intermediate */
    @Column(name = "stop_sequence", nullable = false)
    private Integer stopSequence;

    @Column(name = "stop_name", nullable = false, length = 100)
    private String stopName;                    // e.g. "Wangdue Phodrang"

    @Column(name = "dzongkhag", length = 100)
    private String dzongkhag;

    @Column(name = "dropPointId", length = 11)
    private Long dropPointId;

    /** Cumulative km from origin to this stop */
    @Column(name = "distance_from_origin_km", precision = 8, scale = 2)
    private BigDecimal distanceFromOriginKm;

    /** Estimated arrival time at this stop (minutes from departure) */
    @Column(name = "eta_minutes")
    private Integer etaMinutes;
}