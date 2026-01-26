package com.goBhutan.adminPanel.theater.dto.hall;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HallSummaryDTO {

    private Long id;
    private String name;
    private Integer totalSeats;
    private String theaterName;
    private Boolean isActive;
}
