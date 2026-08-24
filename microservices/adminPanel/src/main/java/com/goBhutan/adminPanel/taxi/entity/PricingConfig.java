package com.goBhutan.adminPanel.taxi.entity;

import com.goBhutan.adminPanel.taxi.enums.TripCategory;
import com.goBhutan.adminPanel.taxi.enums.TripMode;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Admin-managed pricing rules.
 * One row per (category + mode) combination.
 * e.g. INTRA + PULL, INTRA + RESERVED, INTER + PULL, INTER + RESERVED
 */
@Entity
@Table(name = "tbl_taxi_pricing_config",
       uniqueConstraints = @UniqueConstraint(columnNames = {"trip_category","trip_mode"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PricingConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "trip_category", nullable = false)
    private TripCategory tripCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "trip_mode", nullable = false)
    private TripMode tripMode;

    // ── Intra fields ──────────────────────────────────────────────────────────

    /** Flat flag-fall fare (intra only) */
    @Column(name = "base_fare", precision = 10, scale = 2)
    private BigDecimal baseFare;

    /** Per-km rate (intra: per trip; inter: per seat per km) */
    @Column(name = "rate_per_km", precision = 10, scale = 2)
    private BigDecimal ratePerKm;

    /** Flat night surcharge added 22:00–05:00 (intra only) */
    @Column(name = "night_surcharge", precision = 10, scale = 2)
    private BigDecimal nightSurcharge;

    // ── Reserved fields (both intra and inter) ────────────────────────────────

    /**
     * Extra % added for exclusivity in Reserved mode.
     * e.g. 20 → 20% on top of base fare.
     */
    @Column(name = "reserved_premium_pct", precision = 5, scale = 2)
    private BigDecimal reservedPremiumPct;

    /**
     * % of total fare charged as deposit at booking time (Reserved only).
     * e.g. 30 → 30% deposit now, 70% on trip completion.
     */
    @Column(name = "deposit_pct", precision = 5, scale = 2)
    private BigDecimal depositPct;

    // ── Platform commission ───────────────────────────────────────────────────

    /** Commission taken by platform. e.g. 15 → 15% */
    @Column(name = "commission_pct", nullable = false, precision = 5, scale = 2)
    private BigDecimal commissionPct;

    /** Max surge multiplier cap. e.g. 2.0 → fare cannot exceed 2× base */
    @Column(name = "max_surge_multiplier", precision = 4, scale = 2)
    private BigDecimal maxSurgeMultiplier;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate @PrePersist
    public void touch() { this.updatedAt = LocalDateTime.now(); }
}
