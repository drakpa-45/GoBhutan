package com.goBhutan.adminPanel.theater.dto.seat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatClassDTO {
    private Long id;
    private String name;
    private String description;
    private Double defaultBasePrice;
}