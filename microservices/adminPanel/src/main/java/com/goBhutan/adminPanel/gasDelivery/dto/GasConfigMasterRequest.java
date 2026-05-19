package com.goBhutan.adminPanel.gasDelivery.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GasConfigMasterRequest {

    @NotBlank(message = "gasType is required")
    private String gasType;

    private Integer quantity;
}
