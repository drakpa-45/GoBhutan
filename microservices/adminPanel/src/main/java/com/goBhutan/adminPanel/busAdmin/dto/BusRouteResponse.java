package com.goBhutan.adminPanel.busAdmin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
@AllArgsConstructor
public class BusRouteResponse {
    private Long id;
    private Long busId;
    private String busNumber;
    private String source;
    private String destination;
    private BigDecimal distance;
    private BigDecimal baseFare;
    private BigDecimal finalFare;
    private Integer estimatedDuration;
    private LocalTime departureTime;
    private Boolean active;
}
