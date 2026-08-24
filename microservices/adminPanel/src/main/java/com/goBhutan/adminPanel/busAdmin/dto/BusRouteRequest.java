package com.goBhutan.adminPanel.busAdmin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.goBhutan.adminPanel.common.json.FlexibleLocalTimeDeserializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
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

    @NotNull(message = "Estimated duration in minutes is required")
    @Min(value = 1, message = "Estimated duration in minutes must be at least 1")
    private Integer estimatedDurationMinutes;

    @NotNull(message = "Departure time is required")
    @JsonFormat(pattern = "HH:mm")
    @JsonDeserialize(using = FlexibleLocalTimeDeserializer.class)
    @Schema(type = "string", example = "13:00", description = "24-hour time, or explicit AM/PM like 1:00 PM")
    private LocalTime departureTime;

    @DecimalMin(value = "0.0", message = "App charges must be zero or greater")
    private BigDecimal appCharges;

    private Boolean active;
}
