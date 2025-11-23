package com.goBhutan.adminPanel.busAdmin.dto;

import lombok.Data;

@Data
public class LockSeatRequest {
    private Long scheduleId;
    private Integer seatNumber;
}
