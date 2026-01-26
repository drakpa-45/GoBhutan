package com.goBhutan.adminPanel.theater.dto.theater;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Theater creation/update DTO")
public class TheaterDTO {

    @NotBlank(message = "Theater name is required")
    @Schema(description = "Theater name", example = "City Cinema")
    private String name;

    @Schema(description = "Theater description", example = "Premium cinema experience in the heart of the city")
    private String description;

    @NotNull(message = "Location ID is required")
    @Schema(description = "Location ID", example = "1")
    private Long locationId;

    @Schema(description = "Admin user ID from Keycloak")
    private String adminUserId;
}