package com.goBhutan.adminPanel.busAdmin.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CancellationRequestDTO {
    @NotNull(message = "Booking ID is required")
    private Long bookingId;

    private String cancellationReason;
}
