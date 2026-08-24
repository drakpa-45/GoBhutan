package com.goBhutan.adminPanel.taxi.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TaxiDriverRequest {

    @NotBlank(message = "Vehicle make is required")
    private String vehicleMake;

    @NotBlank(message = "Vehicle model is required")
    private String vehicleModel;

    @NotBlank(message = "Vehicle color is required")
    private String vehicleColor;

    @NotNull(message = "Total seats is required")
    @Min(1) @Max(50)
    private Integer totalSeats;

    @NotBlank(message = "License number is required")
    private String licenseNumber;

    @NotBlank(message = "Registration number is required")
    private String registrationNumber;

    @NotBlank(message = "Driver name is required")
    private String driverName;

    @NotNull(message = "phone number is required")
    @Min(8) @Max(8)
    private Integer phoneNumber;
}
