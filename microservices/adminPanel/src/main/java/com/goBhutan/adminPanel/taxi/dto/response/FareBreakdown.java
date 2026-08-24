package com.goBhutan.adminPanel.taxi.dto.response;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class FareBreakdown {
    private BigDecimal baseFare;
    private BigDecimal distanceCharge;
    private BigDecimal nightSurcharge;
    private BigDecimal reservedPremium;
    private BigDecimal surgeMultiplier;
    private BigDecimal totalFare;
    private BigDecimal depositAmount;      // null for Pull
    private BigDecimal balanceAmount;      // null for Pull
    private BigDecimal commissionAmount;
    private BigDecimal driverNetAmount;
    private Integer    seatsBooked;        // inter only
    private BigDecimal farePerSeat;        // inter only
}
