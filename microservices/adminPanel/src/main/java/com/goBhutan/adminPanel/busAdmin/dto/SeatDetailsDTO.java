package com.goBhutan.adminPanel.busAdmin.dto;

import com.goBhutan.adminPanel.busAdmin.enums.SeatType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeatDetailsDTO {
    private String seatNumber;
    private SeatType seatType;
    private BigDecimal fare;
}
