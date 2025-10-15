package com.goBhutan.adminPanel.hotel.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public record HotelResponseDTO(
        Long id,
        String name,
        String description,
        String address,
        String city,
        String state,
        String country,
        String postalCode,
        String phoneNumber,
        String email,
        String website,
        Integer starRating,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Set<AmenityDTO> amenities,
        List<RoomDTO> rooms
) {}
