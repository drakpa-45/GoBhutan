package com.goBhutan.adminPanel.theater.dto.seat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatDTO {

    private Long id;
    private String rowName;
    private Integer seatNumber;
    private String seatIdentifier;

    private Long seatClassId;
    private String seatClassName;

    private Long hallId;
    private String hallName;

    private Boolean isBlocked;
    private String blockReason;

    private Instant createdAt;
    private Instant updatedAt;

    private Double basePrice;
}
