package com.goBhutan.adminPanel.taxi.dto.request;

import com.goBhutan.adminPanel.taxi.enums.TripCategory;
import com.goBhutan.adminPanel.taxi.enums.TripMode;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PricingConfigRequest {

    @NotNull(message = "Trip category is required")
    private TripCategory tripCategory;

    @NotNull(message = "Trip mode is required")
    private TripMode tripMode;

    // ── Intra fields ──────────────────────────────────────────────────────────

    @DecimalMin(value = "0.00", message = "Base fare cannot be negative")
    private BigDecimal baseFare;

    @DecimalMin(value = "0.00", message = "Rate per km cannot be negative")
    private BigDecimal ratePerKm;

    @DecimalMin(value = "0.00", message = "Night surcharge cannot be negative")
    private BigDecimal nightSurcharge;

    // ── Reserved fields ───────────────────────────────────────────────────────

    @DecimalMin(value = "0.00")
    @DecimalMax(value = "100.00", message = "Reserved premium cannot exceed 100%")
    private BigDecimal reservedPremiumPct;

    @DecimalMin(value = "0.00")
    @DecimalMax(value = "100.00", message = "Deposit percentage cannot exceed 100%")
    private BigDecimal depositPct;

    // ── Platform commission ───────────────────────────────────────────────────

    @NotNull(message = "Commission percentage is required")
    @DecimalMin(value = "0.00")
    @DecimalMax(value = "100.00", message = "Commission cannot exceed 100%")
    private BigDecimal commissionPct;

    @DecimalMin(value = "1.00", message = "Surge multiplier must be at least 1.0")
    @DecimalMax(value = "5.00", message = "Surge multiplier cap cannot exceed 5.0")
    private BigDecimal maxSurgeMultiplier;
}
