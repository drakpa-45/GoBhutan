package com.goBhutan.adminPanel.busAdmin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BusRouteMapResponse {
    private Long id;
    private Long busId;
    private String busNumber;
    private Long routeId;
    private String source;
    private String destination;
    private LocalTime departureTime;
    private BigDecimal fare;
    private Integer estimatedDuration;
    private Boolean active;
}
