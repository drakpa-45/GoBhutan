package com.goBhutan.adminPanel.busAdmin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ScheduleRequest {
    @NotNull(message = "Bus ID is required")
    private Long busId;

    @NotNull(message = "Route ID is required")
    private Long routeId;

    @NotNull(message = "Departure time is required")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime departureTime;

    @NotNull(message = "Arrival time is required")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime arrivalTime;

}
