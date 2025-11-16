package com.goBhutan.adminPanel.busAdmin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AvailableSeatsResponseDTO {

    private Long scheduleId;
    private String busNumber;
    private Integer totalSeats;
    private Integer availableSeats;
    private List<BusSeatAvailabilityDTO> seats;
}
