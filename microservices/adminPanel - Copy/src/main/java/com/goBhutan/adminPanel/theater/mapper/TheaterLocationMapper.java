package com.goBhutan.adminPanel.theater.mapper;

import com.goBhutan.adminPanel.theater.dto.theater.TheaterLocationDTO;
import com.goBhutan.adminPanel.theater.dto.theater.TheaterLocationResponseDTO;
import com.goBhutan.adminPanel.theater.entity.TheaterLocation;

public class TheaterLocationMapper {

    public static TheaterLocation toEntity(TheaterLocationDTO dto) {
        if (dto == null) return null;

        TheaterLocation location = new TheaterLocation();
        location.setDzongkhag(dto.getDzongkhag());
        location.setThromdoe(dto.getThromdoe());
        location.setAddress(dto.getAddress());

        return location;
    }

    public static TheaterLocationResponseDTO toResponseDTO(TheaterLocation location) {
        if (location == null) return null;

        TheaterLocationResponseDTO dto = new TheaterLocationResponseDTO();
        dto.setId(location.getId());
        dto.setDzongkhag(location.getDzongkhag());
        dto.setThromdoe(location.getThromdoe());
        dto.setAddress(location.getAddress());
        dto.setCreatedAt(location.getCreatedAt());
        dto.setTheaterCount(location.getTheaters() != null ? location.getTheaters().size() : 0);

        return dto;
    }

    public static void updateEntityFromDTO(TheaterLocation location, TheaterLocationDTO dto) {
        if (location == null || dto == null) return;

        if (dto.getDzongkhag() != null) {
            location.setDzongkhag(dto.getDzongkhag());
        }
        if (dto.getThromdoe() != null) {
            location.setThromdoe(dto.getThromdoe());
        }
        if (dto.getAddress() != null) {
            location.setAddress(dto.getAddress());
        }
    }
}