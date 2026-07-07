package com.goBhutan.adminPanel.taxi.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Always contains the LATEST location of each driver.
 * One row per driver — upserted (not inserted) every 30 seconds.
 * Used for:
 *  - Nearest driver matching (Pull mode)
 *  - Live map display on passenger app
 *  - Driver online/offline detection (lastUpdatedAt stale > 2 min = offline)
 */
@Entity
@Table(name = "tbl_taxi_driver_location",
       indexes = {
           @Index(name = "idx_dl_driver",  columnList = "driver_id", unique = true),
           @Index(name = "idx_dl_active",  columnList = "is_online, last_updated_at"),
           @Index(name = "idx_dl_latlong", columnList = "latitude, longitude")
       })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DriverLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "driver_id", nullable = false, unique = true)
    private String driverId;

    @Column(name = "latitude", nullable = false, precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(name = "longitude", nullable = false, precision = 10, scale = 7)
    private BigDecimal longitude;

    /** Bearing in degrees 0–360, so passenger map can rotate the car icon */
    @Column(name = "bearing", precision = 6, scale = 2)
    private BigDecimal bearing;

    /** km/h — useful for ETA and to detect if driver is stuck */
    @Column(name = "speed_kmh", precision = 6, scale = 2)
    private BigDecimal speedKmh;

    /** Driver is online and accepting rides */
    @Column(name = "is_online", nullable = false)
    private Boolean isOnline = false;

    /** Current trip this driver is on (null = available) */
    @Column(name = "current_booking_id")
    private Long currentBookingId;

    /** Dzongkhag derived from coordinates — used for intra-dzongkhag matching */
    @Column(name = "current_dzongkhag")
    private String currentDzongkhag;

    @Column(name = "last_updated_at", nullable = false)
    private LocalDateTime lastUpdatedAt;

    @PrePersist @PreUpdate
    public void touch() { this.lastUpdatedAt = LocalDateTime.now(); }
}
