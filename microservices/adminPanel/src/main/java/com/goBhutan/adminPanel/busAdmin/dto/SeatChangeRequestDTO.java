package com.goBhutan.adminPanel.busAdmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatChangeRequestDTO {
    @NotNull(message = "Booking ID is required")
    private Long bookingId;

    @NotBlank(message = "Old seat number is required")
    private String oldSeatNumber;

    @NotBlank(message = "New seat number is required")
    private String newSeatNumber;
}
