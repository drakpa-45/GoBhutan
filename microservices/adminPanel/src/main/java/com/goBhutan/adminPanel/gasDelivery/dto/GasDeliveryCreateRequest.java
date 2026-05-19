package com.goBhutan.adminPanel.gasDelivery.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GasDeliveryCreateRequest {

    @NotBlank(message = "Mobile Number is required")
    private String mobileNumber;

    private String customerRemarks;

    @Valid
    @NotEmpty(message = "At least one gas item is required")
    private List<GasDeliveryItemRequest> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GasDeliveryItemRequest {

        @NotNull(message = "gasConfigId is required")
        private Long gasConfigId;

        @NotNull(message = "quantity is required")
        @Positive(message = "quantity must be greater than zero")
        private Integer quantity;
    }
}
