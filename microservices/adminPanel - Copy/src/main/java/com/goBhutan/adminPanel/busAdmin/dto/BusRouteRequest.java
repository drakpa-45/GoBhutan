package com.goBhutan.adminPanel.busAdmin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
public class BusRouteRequest {
    @NotNull(message = "Bus ID is required")
    private Long busId;

    @NotBlank(message = "Source is required")
    private String source;

    @NotBlank(message = "Destination is required")
    private String destination;

    @NotNull(message = "Distance is required")
    @DecimalMin(value = "0.1", message = "Distance must be greater than 0")
    private BigDecimal distance;

    @NotNull(message = "Base fare is required")
    @DecimalMin(value = "0.1", message = "Base fare must be greater than 0")
    private BigDecimal baseFare;

    private Integer estimatedDuration; // minutes

    @NotNull(message = "Departure time is required")
    @JsonFormat(pattern = "HH:mm")
    @Schema(type = "string", example = "10:30")
    private LocalTime departureTime; // formatted as HH:mm

    private BigDecimal customFare;

    private Boolean active;
}
