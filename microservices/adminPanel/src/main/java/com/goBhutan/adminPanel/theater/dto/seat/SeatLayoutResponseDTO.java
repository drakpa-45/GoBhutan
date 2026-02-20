package com.goBhutan.adminPanel.theater.dto.seat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatLayoutResponseDTO {

    private Long hallId;
    private String hallName;
    private Integer totalSeats;
    private Integer blockedSeats;
    private Integer availableSeats;
    private List<RowLayout> rows;
    private Map<String, Integer> seatClassCounts;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RowLayout {
        private String rowName;
        private List<SeatDTO> seats;
        private Integer totalSeats;
        private Integer blockedSeats;
    }
}