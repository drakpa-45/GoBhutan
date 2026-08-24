package com.goBhutan.adminPanel.theater.dto.search;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
public class MovieScreeningResponseDTO {

    // Theater info
    private Long theaterId;
    private String theaterName;
    private String theaterLocation;

    // Halls in this theater screening the movie
    private List<HallScreeningDTO> halls;

    @Data
    @Builder
    public static class HallScreeningDTO {
        private Long hallId;
        private String hallName;
        private Integer totalSeats;

        // All show times for this hall
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