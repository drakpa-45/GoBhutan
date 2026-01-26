package com.goBhutan.adminPanel.theater.dto.seat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to block/unblock a seat")
public class SeatBlockRequestDTO {

    @NotNull(message = "Block status is required")
    @Schema(description = "Whether to block the seat", example = "true", required = true)
    private Boolean block;

    @Schema(description = "Reason for blocking", example = "Broken seat - needs repair")
    private String reason;
}