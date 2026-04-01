package com.goBhutan.adminPanel.theater.dto.hall;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HallResponseDTO {

    private Long id;
    private String name;
    private Integer totalSeats;
    private Long theaterId;
    private String theaterName;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
    private Integer seatCount;
}