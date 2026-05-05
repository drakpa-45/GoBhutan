// SeatLockResponseDTO.java
package com.goBhutan.adminPanel.theater.dto.seat;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Builder
public class SeatLockResponseDTO {

    private Long seatId;
    private String seatIdentifier;   // e.g. "A1"

    private Long screenId;

    private Long hallId;
    private String hallName;

    private Long seatClassId;
    private String seatClassName;    // e.g. "VIP", "Economy"
    private BigDecimal seatPrice;    // from seat.basePrice

    private String status;           // "LOCKED" | "UNLOCKED"
    private LocalDateTime expiresAt;
    private long secondsRemaining;
}