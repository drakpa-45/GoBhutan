package com.goBhutan.adminPanel.theater.dto.theater;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Theater update DTO - all fields optional")
public class TheaterUpdateDTO {

    @Schema(description = "Theater name", example = "Updated Cinema Name")
    private String name;

    @Schema(description = "Theater description")
    private String description;

    @Schema(description = "Location ID", example = "2")
    private Long locationId;

    @Schema(description = "Admin user ID from Keycloak")
    private String adminUserId;

    @Schema(description = "Active status", example = "true")
    private Boolean isActive;
}