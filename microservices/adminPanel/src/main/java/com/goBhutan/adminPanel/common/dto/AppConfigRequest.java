package com.goBhutan.adminPanel.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AppConfigRequest {

    @NotBlank(message = "CONFIG_FOR is required")
    private String configFor;

    @NotBlank(message = "CONFIG_VALUE is required")
    private String configValue;

    private Boolean active;
}
