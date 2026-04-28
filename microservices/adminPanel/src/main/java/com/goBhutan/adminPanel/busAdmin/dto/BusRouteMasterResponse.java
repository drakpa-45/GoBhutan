package com.goBhutan.adminPanel.busAdmin.dto;

import java.time.LocalDateTime;

public record BusRouteMasterResponse(
        Long id,
        String routeName,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
