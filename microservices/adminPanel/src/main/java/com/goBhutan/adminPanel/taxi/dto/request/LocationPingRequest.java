package com.goBhutan.adminPanel.taxi.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;

/** Sent by driver app every 30 seconds */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LocationPingRequest {

    @NotNull
    private String driverId;

    @NotNull
    private BigDecimal latitude;

    @NotNull
    private BigDecimal longitude;

    /** 0–360 degrees */
    private BigDecimal bearing;

    /** km/h from device GPS */
    private BigDecimal speedKmh;

    /** Driver toggled online/offline */
    private Boolean isOnline;

    /** Active booking if driver is mid-trip (null = available) */
    private Long currentBookingId;
}
