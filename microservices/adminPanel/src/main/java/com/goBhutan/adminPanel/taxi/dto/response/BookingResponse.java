package com.goBhutan.adminPanel.taxi.dto.response;

import com.goBhutan.adminPanel.taxi.enums.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class BookingResponse {
    private Long          bookingId;
    private TripCategory  tripCategory;
    private TripMode      tripMode;
    private TaxiBookingStatus bookingStatus;
    private TaxiPaymentStatus paymentStatus;
    private TaxiPaymentMethod paymentMethod;

    private String riderName;
    private String riderPhone;
    private Boolean bookForOther;

    private String riderPickupAddress;
    private String dropOffAddress;

    // Inter
    private Long    interRouteId;
    private Integer seatsBooked;
    private Integer availableSeatsAfterBooking;

    // Scheduling
    private LocalDateTime scheduledPickupTime;

    // Fare
    private FareBreakdown fareBreakdown;

    // Payment action required
    private BigDecimal amountDueNow;   // deposit for reserved; full fare for pull
    private String     paymentMessage; // e.g. "Deposit paid. Balance of Nu 420 due on trip completion."

    private LocalDateTime createdAt;
}
