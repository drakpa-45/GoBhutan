package com.goBhutan.adminPanel.hotel.dto;

import com.goBhutan.adminPanel.hotel.entity.Amenity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AmenityDTO {
    @Schema(description = "Amenity ID (for updates, null for new)")
    private Long id;

    @NotBlank(message = "Amenity name is required")
    @Schema(description = "Amenity name")
    private String name;

    @Schema(description = "Amenity description")
    private String description;

    @Schema(description = "Icon class for UI")
    private String iconClass;

    @NotNull(message = "Category is required")
    @Schema(description = "Amenity category")
    private Amenity.AmenityCategory category;
}