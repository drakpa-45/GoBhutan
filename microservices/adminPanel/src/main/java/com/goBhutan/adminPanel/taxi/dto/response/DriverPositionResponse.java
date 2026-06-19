package com.goBhutan.adminPanel.taxi.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Returned to passenger app — shows driver on map */
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class DriverPositionResponse {
    private Long        driverId;
    private BigDecimal  latitude;
    private BigDecimal  longitude;
    private BigDecimal  bearing;
    private BigDecimal  speedKmh;
    private Boolean     isOnline;
    private Long        currentBookingId;
    private String      currentDzongkhag;
    private LocalDateTime lastUpdatedAt;
    /** seconds since last ping — client shows "offline" if > 90 */
    private Long        secondsSinceLastPing;
}
