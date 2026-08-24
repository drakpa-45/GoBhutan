package com.goBhutan.adminPanel.taxi.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InitialLocationRequest {

    @NotNull
    private String driverId;

    @NotNull
    private BigDecimal latitude;

    @NotNull
    private BigDecimal longitude;

    private BigDecimal bearing;
    private String     dzongkhag;
}
