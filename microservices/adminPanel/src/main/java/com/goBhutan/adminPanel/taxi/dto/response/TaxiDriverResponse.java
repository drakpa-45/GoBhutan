package com.goBhutan.adminPanel.taxi.dto.response;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class TaxiDriverResponse {
    private Long        id;
    private String        driverId;
    private String      vehicleMake;
    private String      vehicleModel;
    private String      vehicleColor;
    private Integer     totalSeats;
    private Integer     phoneNumber;
    private String      licenseNumber;
    private String      registrationNumber;
    private String      driverName;
    private Boolean     isOnline;
    private List<VehicleImageResponse> images;   // ordered list, unlimited
    private LocalDateTime createdAt;
}
