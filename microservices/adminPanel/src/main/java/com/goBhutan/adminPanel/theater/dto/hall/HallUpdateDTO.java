package com.goBhutan.adminPanel.theater.dto.hall;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Hall update DTO - all fields optional")
public class HallUpdateDTO {

    @Schema(description = "Hall name", example = "Updated Hall Name")
    private String name;

    @Min(value = 1, message = "Total seats must be at least 1")
    @Schema(description = "Total number of seats", example = "150")
    private Integer totalSeats;

    @Schema(description = "Theater ID", example = "2")
    private Long theaterId;

    @Schema(description = "Active status", example = "true")
    private Boolean isActive;
}