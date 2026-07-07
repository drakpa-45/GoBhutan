package com.goBhutan.adminPanel.taxi.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

/** One entry in the "find nearest driver" list (Pull mode) */
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class NearbyDriverResponse {
    // Location
    private String       driverId;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal bearing;
    private BigDecimal distanceKm;
    private Integer    etaMinutes;
    private String     currentDzongkhag;

    // Driver details
    private String     driverName;
    private Integer     contactNumber;

    // Taxi details
    private String     vehicleMake;
    private String     vehicleModel;
    private String     vehicleColor;
    private Integer    totalSeats;
    private String     registrationNumber;

    // Vehicle images
    private List<VehicleImageResponse> images;
}
