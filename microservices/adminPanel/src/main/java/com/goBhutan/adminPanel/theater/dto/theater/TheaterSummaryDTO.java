package com.goBhutan.adminPanel.theater.dto.theater;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TheaterSummaryDTO {

    private Long id;
    private String name;
    private String locationName;
    private Boolean isActive;
    private Integer hallCount;
}