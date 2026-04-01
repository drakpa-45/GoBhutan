package com.goBhutan.adminPanel.theater.mapper;

import com.goBhutan.adminPanel.theater.dto.hall.HallDTO;
import com.goBhutan.adminPanel.theater.dto.hall.HallResponseDTO;
import com.goBhutan.adminPanel.theater.dto.hall.HallSummaryDTO;
import com.goBhutan.adminPanel.theater.dto.hall.HallUpdateDTO;
import com.goBhutan.adminPanel.theater.entity.Hall;

public class HallMapper {

    /**
     * Convert HallDTO to Hall Entity
     */
    public static Hall toEntity(HallDTO dto) {
        if (dto == null) return null;

        Hall hall = new Hall();
        hall.setName(dto.getName());
        hall.setTotalSeats(dto.getTotalSeats());

        // Theater will be set in the service layer
        return hall;
    }

    /**
     * Convert Hall Entity to HallResponseDTO
     */
    public static HallResponseDTO toResponseDTO(Hall hall) {
        if (hall == null) return null;

        HallResponseDTO dto = new HallResponseDTO();
        dto.setId(hall.getId());
        dto.setName(hall.getName());
        dto.setTotalSeats(hall.getTotalSeats());
        dto.setIsActive(hall.getIsActive());
        dto.setCreatedAt(hall.getCreatedAt());
        dto.setUpdatedAt(hall.getUpdatedAt());

        // Set theater details
        if (hall.getTheater() != null) {
            dto.setTheaterId(hall.getTheater().getId());
            dto.setTheaterName(hall.getTheater().getName());
        }

        // Set actual seat count
        dto.setSeatCount(hall.getSeats() != null ? hall.getSeats().size() : 0);

        return dto;
    }

    /**
     * Convert Hall Entity to HallSummaryDTO
     */
    public static HallSummaryDTO toSummaryDTO(Hall hall) {
        if (hall == null) return null;

        HallSummaryDTO dto = new HallSummaryDTO();
        dto.setId(hall.getId());
        dto.setName(hall.getName());
        dto.setTotalSeats(hall.getTotalSeats());
        dto.setIsActive(hall.getIsActive());

        if (hall.getTheater() != null) {
            dto.setTheaterName(hall.getTheater().getName());
        }

        return dto;
    }

    /**
     * Update existing Hall Entity from HallUpdateDTO
     * Only updates non-null fields
     */
    public static void updateEntityFromDTO(Hall hall, HallUpdateDTO dto) {
        if (hall == null || dto == null) return;

        if (dto.getName() != null && !dto.getName().isBlank()) {
            hall.setName(dto.getName());
        }
        if (dto.getTotalSeats() != null) {
            hall.setTotalSeats(dto.getTotalSeats());
        }
        if (dto.getIsActive() != null) {
            hall.setIsActive(dto.getIsActive());
        }

        // Theater update will be handled in service layer
    }
}
