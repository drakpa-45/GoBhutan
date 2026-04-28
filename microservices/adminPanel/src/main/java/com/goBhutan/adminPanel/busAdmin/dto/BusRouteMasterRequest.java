package com.goBhutan.adminPanel.busAdmin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BusRouteMasterRequest {

    @NotBlank(message = "Route name is required")
    private String routeName;
}
