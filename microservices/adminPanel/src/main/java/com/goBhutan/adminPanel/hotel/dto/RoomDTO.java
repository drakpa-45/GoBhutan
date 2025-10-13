package com.goBhutan.adminPanel.hotel.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

public record RoomDTO(
        Long id,
        String roomNumber,
        RoomTypeDTO roomType,
        Integer floor,
        BigDecimal basePrice,
        Integer maxOccupancy,
        String status,
        LocalDate currentCheckInDate,
        LocalDate currentCheckOutDate,
        Boolean isActive,
        String description,
        Set<AmenityDTO> amenities
) {}
