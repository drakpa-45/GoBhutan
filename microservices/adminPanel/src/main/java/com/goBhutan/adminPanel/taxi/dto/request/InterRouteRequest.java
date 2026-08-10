package com.goBhutan.adminPanel.taxi.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InterRouteRequest {

    @NotBlank
    private String originDzongkhag;

    @NotBlank
    private String originAddress;

    @NotBlank
    private String destinationDzongkhag;

    @NotBlank
    private String destinationAddress;

    @NotNull(message = "Origin dzongkhag is required")
    private Long originDzongkhagId;        // FK to tbl_master_dzongkhag

    @NotNull(message = "Destination dzongkhag is required")
    private Long destinationDzongkhagId;   // FK to tbl_master_dzongkhag

    @NotNull(message = "Rate per km per seat is required")
    private BigDecimal ratePerKmPerSeat;   // driver sets the price

    @NotNull(message = "Total seats is required")
    private Integer totalSeats;

    @NotNull(message = "Departure time is required")
    private LocalDateTime departureTime;

    /** Driver selects drop points from filtered master list */
    private List<RouteStopRequest> intermediateStops;
    /**
     * Ordered list of intermediate stops between origin and destination.
     * Driver adds stops where they will pick up or drop off passengers.
     * Origin and destination are NOT included here — they come from above fields.
     *
     * Example for Thimphu → Samtse:
     * [
     *   { "stopName": "Chuzom",           "distanceFromOriginKm": 30,  "etaMinutes": 45  },
     *   { "stopName": "Wangdue Phodrang", "distanceFromOriginKm": 70,  "etaMinutes": 105 },
     *   { "stopName": "Tsirang",          "distanceFromOriginKm": 120, "etaMinutes": 180 },
     *   { "stopName": "Sarpang",          "distanceFromOriginKm": 160, "etaMinutes": 240 }
     * ]
     */
}