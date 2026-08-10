package com.goBhutan.adminPanel.taxi.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RouteStopRequest {

    @NotBlank
    private String stopName;

    private String dzongkhag;

    @NotNull
    private BigDecimal distanceFromOriginKm;

    private Integer etaMinutes;

    @NotNull(message = "Drop point is required")
    private Long dropPointId;
}