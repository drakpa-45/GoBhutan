package com.goBhutan.adminPanel.busAdmin.dto;

import com.goBhutan.adminPanel.busAdmin.enums.RecurrenceType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.DayOfWeek;
import java.util.Set;

@Data
public class BusRegistrationRequest {
    @NotBlank(message = "Bus number is required")
    private String busNumber;
    @NotBlank(message = "Bus name is required")
    private String busName;
    @NotBlank(message = "Bus type is required")
    private String busType;
    @NotNull(message = "Total seats is required")
    @Min(value = 1, message = "Total seats must be at least 1")
    private Integer totalSeats;
    private String description;
    private String amenities;
    private String layoutType;
    private RecurrenceType recurrenceType;
    private Set<DayOfWeek> operatingDays;
}
