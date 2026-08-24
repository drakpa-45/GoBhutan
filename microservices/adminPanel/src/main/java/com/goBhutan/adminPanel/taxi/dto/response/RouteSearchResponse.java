package com.goBhutan.adminPanel.taxi.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class RouteSearchResponse {

    private Long            routeId;
    private String          driverId;
    private LocalDateTime   departureTime;
    private Integer         availableSeats;
    private BigDecimal      ratePerKmPerSeat;

    // Full route info
    private String          fullRouteOrigin;
    private String          fullRouteDestination;
    private BigDecimal      fullRouteDistanceKm;

    // Passenger's segment
    private StopDetail      boardingStop;      // where passenger boards
    private StopDetail      alightingStop;     // where passenger alights
    private BigDecimal      segmentDistanceKm; // distance for their segment only
    private BigDecimal      estimatedFarePerSeat; // pre-calculated for display

    // All stops on this route — shown so passenger can see full path
    private List<StopDetail> allStops;

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class StopDetail {
        private Long      stopId;
        private String    stopName;
        private String    dzongkhag;
        private BigDecimal distanceFromOriginKm;
        private Integer   etaMinutes;
        private Integer   stopSequence;
    }
}