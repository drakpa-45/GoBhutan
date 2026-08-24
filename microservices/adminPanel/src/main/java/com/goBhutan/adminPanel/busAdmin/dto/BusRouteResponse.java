package com.goBhutan.adminPanel.busAdmin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BusRouteResponse {
    private Long id;
    private Long busId;
    private String busNumber;
    private String source;
    private String destination;
    private BigDecimal distance;
    private BigDecimal baseFare;
    private BigDecimal appCharges;
    private BigDecimal finalFare;
    private Integer estimatedDurationMinutes;
    @JsonFormat(pattern = "HH:mm")
    private LocalTime departureTime;
    private String departureTimeDisplay;
    @JsonFormat(pattern = "HH:mm")
    private LocalTime checkInTime;
    private String checkInTimeDisplay;
    private Boolean active;
    private Long scheduleId;
    private LocalDate travelDate;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime scheduledCheckInTime;
    private String scheduledCheckInTimeDisplay;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime scheduledDepartureTime;
    private String scheduledDepartureTimeDisplay;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime scheduledArrivalTime;
    private String scheduledArrivalTimeDisplay;
    private Integer availableSeats;
}
