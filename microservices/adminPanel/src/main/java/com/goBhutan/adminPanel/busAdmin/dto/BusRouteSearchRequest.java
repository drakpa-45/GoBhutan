package com.goBhutan.adminPanel.busAdmin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BusRouteSearchRequest {
    @NotBlank(message = "Source is required")
    private String source;

    @NotBlank(message = "Destination is required")
    private String destination;
}
