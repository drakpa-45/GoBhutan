package com.goBhutan.adminPanel.theater.dto.screening;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScreeningDTO {

    private Long id;
    private String movieName;
    private LocalDate screeningDate;
    private LocalTime startTime;
    private String trailerLink;
    private String theaterName;
    private String posterImage;
    private Long hallId;
    private String hallName;
    private Boolean isActive;
}
