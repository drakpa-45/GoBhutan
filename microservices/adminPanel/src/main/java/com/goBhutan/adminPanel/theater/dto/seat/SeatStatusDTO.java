package com.goBhutan.adminPanel.theater.dto.seat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatStatusDTO {
    private Long id;
    private String statusName;
    private String description;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
}
