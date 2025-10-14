package com.goBhutan.adminPanel.hotel.dto;

public record RoomTypeDTO(
        Long id,
        String name,
        String description,
        Integer bedCount,
        String bedType,
        String roomSize
) {}
