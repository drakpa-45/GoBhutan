package com.goBhutan.adminPanel.theater.dto.hall;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Hall creation DTO")
public class HallDTO {

    @NotBlank(message = "Hall name is required")
    @Schema(description = "Hall name", example = "Hall 1", required = true)
    private String name;

    @NotNull(message = "Total seats is required")
    @Min(value = 1, message = "Total seats must be at least 1")
    @Schema(description = "Total number of seats", example = "100", required = true)
    private Integer totalSeats;

    @NotNull(message = "Theater ID is required")
    @Schema(description = "Theater ID", example = "1", required = true)
    private Long theaterId;
}