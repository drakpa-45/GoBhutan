package com.goBhutan.adminPanel.taxi.entity;

import com.goBhutan.adminPanel.taxi.enums.*;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * One booking record.
 * Covers all four modes: INTRA+PULL, INTRA+RESERVED, INTER+PULL, INTER+RESERVED.
 *
 * Key design decisions:
 *  - passengerId    = the person who placed the booking (pays)
 *  - riderPhone     = who is actually riding (may differ from passenger for "book for other")
 *  - riderPickupLat/Lng = where the RIDER should be picked up (Google Maps pin)
 *  - interRouteId   = set only for inter-dzongkhag bookings
 *  - seatsBooked    = 1 for intra; 1–N for inter PULL; all seats for inter RESERVED
 */
@Entity
@Table(name = "tbl_taxi_booking")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TaxiBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Who is booking / who is riding ───────────────────────────────────────

    /** The app user making the booking (the payer) */
    @Column(name = "passenger_id", nullable = false)
    private Long passengerId;

    /** Name of the person who will actually ride (may be same as passenger) */
    @Column(name = "rider_name")
    private String riderName;

    /** Phone of the rider — used to notify them of driver arrival */
    @Column(name = "rider_phone")
    private String riderPhone;

    /** true = booked for someone else; pickup location is riderPickupLat/Lng */
    @Column(name = "book_for_other", nullable = false)
    private Boolean bookForOther = false;

    // ── Trip classification ───────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(name = "trip_category", nullable = false)
    private TripCategory tripCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "trip_mode", nullable = false)
    private TripMode tripMode;

    // ── Location ──────────────────────────────────────────────────────────────

    /** Passenger's or rider's actual pickup point (Google Maps pin) */
    @Column(name = "rider_pickup_lat", precision = 10, scale = 7)
    private BigDecimal riderPickupLat;

    @Column(name = "rider_pickup_lng", precision = 10, scale = 7)
    private BigDecimal riderPickupLng;

    @Column(name = "rider_pickup_address")
    private String riderPickupAddress;

    @Column(name = "drop_off_lat", precision = 10, scale = 7)
    private BigDecimal dropOffLat;

    @Column(name = "drop_off_lng", precision = 10, scale = 7)
    private BigDecimal dropOffLng;

    @Column(name = "drop_off_address")
    private String dropOffAddress;

    // ── Inter-dzongkhag specifics ─────────────────────────────────────────────

    /** FK to InterRoute (null for intra-dzongkhag bookings) */
    @Column(name = "inter_route_id")
    private Long interRouteId;

    /** Number of seats booked on the route (inter only; always 1 for intra) */
    @Column(name = "seats_booked")
    private Integer seatsBooked;

    // ── Driver ───────────────────────────────────────────────────────────────

    @Column(name = "driver_id")
    private Long driverId;

    // ── Scheduling (Reserved only) ────────────────────────────────────────────

    /** Scheduled pickup time (Reserved mode only) */
    @Column(name = "scheduled_pickup_time")
    private LocalDateTime scheduledPickupTime;

    // ── Fare breakdown (all computed by FareCalculatorService) ────────────────

    @Column(name = "base_fare", precision = 10, scale = 2)
    private BigDecimal baseFare;

    @Column(name = "distance_charge", precision = 10, scale = 2)
    private BigDecimal distanceCharge;

    @Column(name = "night_surcharge", precision = 10, scale = 2)
    private BigDecimal nightSurcharge;

    @Column(name = "reserved_premium", precision = 10, scale = 2)
    private BigDecimal reservedPremium;

    @Column(name = "surge_multiplier", precision = 4, scale = 2)
    private BigDecimal surgeMultiplier;

    @Column(name = "total_fare", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalFare;

    @Column(name = "deposit_amount", precision = 10, scale = 2)
    private BigDecimal depositAmount;

    @Column(name = "balance_amount", precision = 10, scale = 2)
    private BigDecimal balanceAmount;

    @Column(name = "commission_amount", precision = 10, scale = 2)
    private BigDecimal commissionAmount;

    @Column(name = "driver_net_amount", precision = 10, scale = 2)
    private BigDecimal driverNetAmount;

    // ── Payment ───────────────────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private TaxiPaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private TaxiPaymentStatus paymentStatus;

    // ── Status & timestamps ───────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(name = "booking_status", nullable = false)
    private TaxiBookingStatus bookingStatus;

    @Column(name = "distance_km", precision = 8, scale = 2)
    private BigDecimal distanceKm;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "driver_accepted_at")
    private LocalDateTime driverAcceptedAt;

    @Column(name = "trip_started_at")
    private LocalDateTime tripStartedAt;

    @Column(name = "trip_ended_at")
    private LocalDateTime tripEndedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.bookingStatus == null) this.bookingStatus = TaxiBookingStatus.PENDING;
        if (this.paymentStatus == null) this.paymentStatus = TaxiPaymentStatus.PENDING;
        if (this.bookForOther == null) this.bookForOther = false;
    }
}
