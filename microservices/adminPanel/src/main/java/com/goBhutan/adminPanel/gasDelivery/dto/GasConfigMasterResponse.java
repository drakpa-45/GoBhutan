package com.goBhutan.adminPanel.gasDelivery.dto;

import java.time.LocalDateTime;

public record GasConfigMasterResponse(
        Long id,
        String gasType,
        Integer quantity,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
