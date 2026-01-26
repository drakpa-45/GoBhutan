package com.goBhutan.adminPanel.hotel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Image data transfer object")
public class ImageDTO {

    @Schema(description = "Image ID", example = "1")
    private Long id;

    @NotBlank(message = "Image URL is required")
    @Schema(description = "Image URL", required = true)
    private String url;

    @Schema(description = "Image title")
    private String title;

    @Schema(description = "Image caption")
    private String caption;

    @Schema(description = "Display order")
    private Integer displayOrder;

    @Schema(description = "Is primary image")
    private Boolean isPrimary;

    @Schema(description = "Created at timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Hotel ID (if image belongs to a hotel)")
    private Long hotelId;

    @Schema(description = "Room ID (if image belongs to a room)")
    private Long roomId;
}