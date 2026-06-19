package com.goBhutan.adminPanel.taxi.dto.response;

import lombok.*;
import java.math.BigDecimal;

/** One entry in the "find nearest driver" list (Pull mode) */
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class NearbyDriverResponse {
    private Long       driverId;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal bearing;
    private BigDecimal distanceKm;       // straight-line from passenger
    private Integer    etaMinutes;       // estimated arrival time
    private String     currentDzongkhag;
}
