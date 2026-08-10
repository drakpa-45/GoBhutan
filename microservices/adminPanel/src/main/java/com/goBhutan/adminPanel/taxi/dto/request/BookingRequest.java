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

    @NotNull
    private Boolean bookForOther;

    private String riderName;
    private String riderPhone;

    @NotNull
    private TripCategory tripCategory;

    @NotNull
    private TripMode tripMode;

    @NotNull
    private BigDecimal riderPickupLat;

    @NotNull
    private BigDecimal riderPickupLng;

    private String riderPickupAddress;
    private BigDecimal dropOffLat;
    private BigDecimal dropOffLng;
    private String dropOffAddress;

    // Intra
    private BigDecimal distanceKm;

    // Inter
    private Long interRouteId;
    private Integer seatsBooked;

    /**
     * Inter-dzongkhag stop-based booking.
     * Passenger boards at this stop (from tbl_taxi_route_stop).
     * If null → boards at route origin.
     */
    private Long boardingStopId;

    /**
     * Passenger alights at this stop.
     * If null → alights at route destination.
     */
    private Long alightingStopId;

    // Reserved
    private LocalDateTime scheduledPickupTime;

    @NotNull
    private TaxiPaymentMethod paymentMethod;
}