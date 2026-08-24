package com.goBhutan.adminPanel.theater.dto.screening;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
public class MovieScreeningResponseDTO {

    private Long theaterId;
    private String theaterName;
    private String theaterDescription;

    // Bhutan-specific location fields
    private Long locationId;
    private String dzongkhag;       // e.g. "Thimphu"
    private String thromdoe;        // e.g. "Thimphu Thromde" (nullable)
    private String address;         // full address
    private String displayLocation; // "Thimphu, Thimphu Thromde" — from getName()

    private List<HallScreeningDTO> halls;

    @Data
    @Builder
    public static class HallScreeningDTO {
        private Long hallId;
        private String hallName;
        private Integer totalSeats;
        private List<ShowTimeDTO> showTimes;
    }

    @Data
    @Builder
    public static class ShowTimeDTO {
        private Long screeningId;
        private LocalDate screeningDate;
        private LocalTime startTime;
        private String trailerLink;
    }
}