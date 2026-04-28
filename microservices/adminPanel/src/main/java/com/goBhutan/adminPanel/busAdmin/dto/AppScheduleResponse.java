package com.goBhutan.adminPanel.busAdmin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AppScheduleResponse {
    private Long scheduleId;
    private Long routeId;
    private Long busId;
    private String busNumber;
    private String busName;
    private String busType;
    private Integer totalSeats;
    private String amenities;
    private String source;
    private String destination;
    private BigDecimal distance;
    private BigDecimal baseFare;
    private BigDecimal appCharges;
    private BigDecimal finalFare;
    private Integer estimatedDurationMinutes;
    private LocalDate travelDate;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime checkInTime;
    private String checkInTimeDisplay;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime departureTime;
    private String departureTimeDisplay;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime arrivalTime;
    private String arrivalTimeDisplay;
    private Integer availableSeats;
}
