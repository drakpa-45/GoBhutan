package com.goBhutan.adminPanel.taxi.dto.response;

import com.goBhutan.adminPanel.taxi.enums.TripCategory;
import com.goBhutan.adminPanel.taxi.enums.TripMode;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class PricingConfigResponse {
    private Long         id;
    private TripCategory tripCategory;
    private TripMode     tripMode;
    private BigDecimal   baseFare;
    private BigDecimal   ratePerKm;
    private BigDecimal   nightSurcharge;
    private BigDecimal   reservedPremiumPct;
    private BigDecimal   depositPct;
    private BigDecimal   commissionPct;
    private BigDecimal   maxSurgeMultiplier;
    private LocalDateTime updatedAt;

    /** Human-readable label e.g. "Intra-dzongkhag — Pull" */
    private String       label;
}
