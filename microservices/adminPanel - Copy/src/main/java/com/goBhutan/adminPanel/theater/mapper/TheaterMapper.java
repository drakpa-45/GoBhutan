package com.goBhutan.adminPanel.theater.mapper;

import com.goBhutan.adminPanel.theater.dto.theater.TheaterDTO;
import com.goBhutan.adminPanel.theater.dto.theater.TheaterResponseDTO;
import com.goBhutan.adminPanel.theater.dto.theater.TheaterSummaryDTO;
import com.goBhutan.adminPanel.theater.dto.theater.TheaterUpdateDTO;
import com.goBhutan.adminPanel.theater.entity.Theater;

public class TheaterMapper {

    /**
     * Convert TheaterDTO to Theater Entity
     */
    public static Theater toEntity(TheaterDTO dto) {
        if (dto == null) return null;

        Theater theater = new Theater();
        theater.setName(dto.getName());
        theater.setDescription(dto.getDescription());
        theater.setAdminUserId(dto.getAdminUserId());

        // Location will be set in the service layer
        return theater;
    }

    /**
     * Convert Theater Entity to TheaterResponseDTO
     */
    public static TheaterResponseDTO toResponseDTO(Theater theater) {
        if (theater == null) return null;

        TheaterResponseDTO dto = new TheaterResponseDTO();
        dto.setId(theater.getId());
        dto.setName(theater.getName());
        dto.setDescription(theater.getDescription());
        dto.setAdminUserId(theater.getAdminUserId());
        dto.setIsActive(theater.getIsActive());
        dto.setCreatedAt(theater.getCreatedAt());
        dto.setUpdatedAt(theater.getUpdatedAt());

        // Set location details
        if (theater.getLocation() != null) {
            dto.setLocationId(theater.getLocation().getId());
            dto.setLocationName(theater.getLocation().getName());
        }

        // Set hall count
        dto.setHallCount(theater.getHalls() != null ? theater.getHalls().size() : 0);

        return dto;
    }

    /**
     * Convert Theater Entity to TheaterSummaryDTO
     */
    public static TheaterSummaryDTO toSummaryDTO(Theater theater) {
        if (theater == null) return null;

        TheaterSummaryDTO dto = new TheaterSummaryDTO();
        dto.setId(theater.getId());
        dto.setName(theater.getName());
        dto.setIsActive(theater.getIsActive());

        if (theater.getLocation() != null) {
            dto.setLocationName(theater.getLocation().getName());
        }

        dto.setHallCount(theater.getHalls() != null ? theater.getHalls().size() : 0);

        return dto;
    }

    /**
     * Update existing Theater Entity from TheaterUpdateDTO
     * Only updates non-null fields
     */
    public static void updateEntityFromDTO(Theater theater, TheaterUpdateDTO dto) {
        if (theater == null || dto == null) return;

        if (dto.getName() != null) {
            theater.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            theater.setDescription(dto.getDescription());
        }
        if (dto.getAdminUserId() != null) {
            theater.setAdminUserId(dto.getAdminUserId());
        }
        if (dto.getIsActive() != null) {
            theater.setIsActive(dto.getIsActive());
        }

        // Location update will be handled in service layer
    }
}