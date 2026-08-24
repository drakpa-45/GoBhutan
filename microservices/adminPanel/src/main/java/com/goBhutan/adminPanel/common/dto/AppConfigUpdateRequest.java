package com.goBhutan.adminPanel.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AppConfigUpdateRequest {

    @NotBlank(message = "CONFIG_VALUE is required")
    private String configValue;

    private Boolean active;
}
