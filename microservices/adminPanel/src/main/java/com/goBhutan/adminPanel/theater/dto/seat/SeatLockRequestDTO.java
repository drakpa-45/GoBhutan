// SeatLockRequestDTO.java
package com.goBhutan.adminPanel.theater.dto.seat;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SeatLockRequestDTO {

    @NotNull
    private Long seatId;        // specific seat

    @NotNull
    private Long screenId;

    @NotNull
    private Long hallId;        // must match seat.hall.id

    @NotNull
    private Long seatClassId;   // must match seat.seatClass.id

    @NotNull
    private String userId;
}