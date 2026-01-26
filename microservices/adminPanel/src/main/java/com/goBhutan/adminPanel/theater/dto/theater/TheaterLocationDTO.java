package com.goBhutan.adminPanel.theater.dto.theater;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Theater location DTO")
public class TheaterLocationDTO {

    @NotBlank(message = "Dzongkhag is required")
    @Schema(description = "Dzongkhag (District)", example = "Thimphu")
    private String dzongkhag;

    @Schema(description = "Thromdoe (Municipality)", example = "Thimphu Throm")
    private String thromdoe;

    @Schema(description = "Detailed address", example = "Chang Lam, Near Clock Tower")
    private String address;
}