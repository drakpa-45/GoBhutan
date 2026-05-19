package com.goBhutan.adminPanel.gasDelivery.dto;

import com.goBhutan.adminPanel.gasDelivery.enums.GasDeliveryStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GasDeliveryAdminStatusRequest {

    @NotNull(message = "status is required")
    private GasDeliveryStatus status;

    private String adminRemarks;

    @Valid
    private List<GasDeliveryAdminItemRequest> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GasDeliveryAdminItemRequest {

        @NotNull(message = "gasConfigId is required")
        private Long gasConfigId;

        @NotNull(message = "quantity is required")
        @PositiveOrZero(message = "quantity must be zero or greater")
        private Integer quantity;
    }
}
