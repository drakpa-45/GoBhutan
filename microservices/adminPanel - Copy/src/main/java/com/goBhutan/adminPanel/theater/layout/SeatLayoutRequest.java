package com.goBhutan.adminPanel.theater.layout;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to configure seat layout for a hall")
public class SeatLayoutRequest {

    @NotNull(message = "Hall ID is required")
    @Schema(description = "Hall ID", example = "1", required = true)
    private Long hallId;

    @NotEmpty(message = "At least one row must be defined")
    @Valid
    @Schema(description = "List of row configurations", required = true)
    private List<RowLayout> rows;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Configuration for a single row")
    public static class RowLayout {

        @NotBlank(message = "Row name is required")
        @Schema(description = "Row identifier", example = "A", required = true)
        private String rowName;

        @NotNull(message = "Seat count is required")
        @Min(value = 1, message = "Seat count must be at least 1")
        @Schema(description = "Number of seats in this row", example = "10", required = true)
        private Integer seatCount;

        @NotNull(message = "Seat class ID is required")
        private Long seatClassId;

        @NotNull(message = "Base price is required")
        @Schema(description = "Base (NORMAL) price for seats in this row", required = true)
        private Double basePrice;
    }
}