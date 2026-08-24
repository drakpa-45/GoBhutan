package com.goBhutan.adminPanel.theater.mapper;

import com.goBhutan.adminPanel.theater.dto.seat.SeatDTO;
import com.goBhutan.adminPanel.theater.entity.Seat;

public class SeatMapper {

    public static SeatDTO toDTO(Seat seat) {
        if (seat == null) return null;

        SeatDTO dto = new SeatDTO();
        dto.setId(seat.getId());
        dto.setRowName(seat.getRowName());
        dto.setSeatNumber(seat.getSeatNumber());
        dto.setSeatIdentifier(seat.getSeatIdentifier());
        if (seat.getSeatClass() != null) {
            dto.setSeatClassId(seat.getSeatClass().getId());
            dto.setSeatClassName(seat.getSeatClass().getName());
        }
        dto.setBasePrice(seat.getBasePrice());
        dto.setIsBlocked(seat.getIsBlocked());
        dto.setBlockReason(seat.getBlockReason());
        dto.setCreatedAt(seat.getCreatedAt());
        dto.setUpdatedAt(seat.getUpdatedAt());

        if (seat.getHall() != null) {
            dto.setHallId(seat.getHall().getId());
            dto.setHallName(seat.getHall().getName());
        }

        return dto;
    }
}