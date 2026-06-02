package com.goBhutan.adminPanel.gasDelivery.dto;

import com.goBhutan.adminPanel.gasDelivery.enums.GasDeliveryStatus;

import java.time.LocalDateTime;
import java.util.List;

public record GasDeliveryResponse(
        Long id,
        String cidNumber,
        String fullName,
        String mobileNumber,
        String userId,
        GasDeliveryStatus status,
        String customerRemarks,
        String adminRemarks,
        List<GasDeliveryItemResponse> items,
        LocalDateTime createdAt,
        String adminUserId,
        LocalDateTime updatedAt
) {
    public record GasDeliveryItemResponse(
            Long id,
            Long gasConfigId,
            String gasType,
            Integer quantity
    ) {
    }
}
