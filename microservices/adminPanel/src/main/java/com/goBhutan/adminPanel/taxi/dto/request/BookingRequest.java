package com.goBhutan.adminPanel.taxi.dto.request;

import com.goBhutan.adminPanel.taxi.enums.TaxiPaymentMethod;
import com.goBhutan.adminPanel.taxi.enums.TripCategory;
import com.goBhutan.adminPanel.taxi.enums.TripMode;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BookingRequest {

    @NotNull
    private String passengerId;

    // ── Book for self or other ────────────────────────────────────────────────

    @NotNull
    private Boolean bookForOther;

    /** Required if bookForOther = true */
    private String riderName;

    /** Required if bookForOther = true — rider will receive SMS notification */
    private String riderPhone;

    // ── Trip type ─────────────────────────────────────────────────────────────

    @NotNull
    private TripCategory tripCategory;

    @NotNull
    private TripMode tripMode;

    // ── Pickup location (always the RIDER's location) ─────────────────────────

    @NotNull
    private BigDecimal riderPickupLat;

    @NotNull
    private BigDecimal riderPickupLng;

    private String riderPickupAddress;

    // ── Drop-off (intra only or for display in inter) ─────────────────────────

    private BigDecimal dropOffLat;
    private BigDecimal dropOffLng;
    private String     dropOffAddress;

    // ── Intra specifics ───────────────────────────────────────────────────────

    /** Actual trip distance in km (calculated by client using Google Maps) */
    private BigDecimal distanceKm;

    // ── Inter specifics ───────────────────────────────────────────────────────

    /** FK to InterRoute — required for inter-dzongkhag bookings */
    private Long interRouteId;

    /** Number of seats to book (inter PULL only; null for inter RESERVED = all seats) */
    private Integer seatsBooked;

    // ── Reserved specifics ────────────────────────────────────────────────────

    /** Required for RESERVED mode */
    private LocalDateTime scheduledPickupTime;

    // ── Payment ───────────────────────────────────────────────────────────────

    @NotNull
    private TaxiPaymentMethod paymentMethod;
}
