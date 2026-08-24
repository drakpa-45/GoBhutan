package com.goBhutan.adminPanel.taxi.controller;


import com.goBhutan.adminPanel.taxi.dto.request.InitialLocationRequest;
import com.goBhutan.adminPanel.taxi.dto.request.LocationPingRequest;
import com.goBhutan.adminPanel.taxi.dto.response.DriverPositionResponse;
import com.goBhutan.adminPanel.taxi.dto.response.NearbyDriverResponse;
import com.goBhutan.adminPanel.taxi.entity.TripLocationHistory;
import com.goBhutan.adminPanel.taxi.service.TaxiLocationTrackingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/taxi/location")
@RequiredArgsConstructor
public class TaxiLocationController {

    private final TaxiLocationTrackingService trackingService;

    // ── Driver → Server ───────────────────────────────────────────────────────

    /**
     * POST /taxi/location/driver/{driverId}/initial
     * Driver sends their location once when they first open the app / go online.
     * Called BEFORE the 30-second ping cycle starts.
     * Sets isOnline = true and stores the first known position.
     */
    /**
     * POST /taxi/location/driver/initial
     * Driver sends their location once when they first open the app.
     * Called BEFORE the 30-second ping cycle starts.
     *
     * Body: { "driverId": 1, "latitude": 27.46, "longitude": 89.64, "bearing": 0.0 }
     */
    @PostMapping("/driver/initial")
    public ResponseEntity<DriverPositionResponse> initialLocation(
            @Valid @RequestBody InitialLocationRequest req) {

        LocationPingRequest ping = LocationPingRequest.builder()
                .driverId(req.getDriverId())
                .latitude(req.getLatitude())
                .longitude(req.getLongitude())
                .bearing(req.getBearing())
                .speedKmh(BigDecimal.ZERO)
                .isOnline(true)
                .currentBookingId(null)
                .build();

        return ResponseEntity.ok(trackingService.handlePing(ping));
    }

    /**
     * POST /api/yaya/location/ping
     * Called by driver app every 30 seconds.
     * Body: { driverId, latitude, longitude, bearing, speedKmh, isOnline, currentBookingId }
     */
    @PostMapping("/ping")
    public ResponseEntity<DriverPositionResponse> ping(
            @Valid @RequestBody LocationPingRequest req) {
        return ResponseEntity.ok(trackingService.handlePing(req));
    }

    /**
     * PATCH /api/yaya/location/driver/{driverId}/online
     * Driver explicitly goes online (starts shift).
     */
    @PatchMapping("/driver/{driverId}/online")
    public ResponseEntity<Void> goOnline(@PathVariable String driverId) {
        trackingService.setOnlineStatus(driverId, true);
        return ResponseEntity.ok().build();
    }

    /**
     * PATCH /api/yaya/location/driver/{driverId}/offline
     * Driver ends shift.
     */
    @PatchMapping("/driver/{driverId}/offline")
    public ResponseEntity<Void> goOffline(@PathVariable String driverId) {
        trackingService.setOnlineStatus(driverId, false);
        return ResponseEntity.ok().build();
    }

    // ── Passenger → Server ────────────────────────────────────────────────────

    /**
     * GET /api/yaya/location/booking/{bookingId}/driver
     * Passenger app polls this every 30 seconds to animate driver on map.
     * Returns 404 if driver not yet assigned.
     */
    @GetMapping("/booking/{bookingId}/driver")
    public ResponseEntity<DriverPositionResponse> getDriverPosition(
            @PathVariable Long bookingId) {
        Optional<DriverPositionResponse> pos = trackingService.getDriverForBooking(bookingId);
        return pos.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/yaya/location/nearby?lat=27.46&lng=89.64&dzongkhag=Thimphu
     * Find available drivers for Pull mode.
     * dzongkhag param is optional — omit for inter-dzongkhag search.
     */
    @GetMapping("/nearby")
    public ResponseEntity<List<NearbyDriverResponse>> nearbyDrivers(
            @RequestParam BigDecimal lat,
            @RequestParam BigDecimal lng,
            @RequestParam(required = false) String dzongkhag) {
        return ResponseEntity.ok(
                trackingService.findNearbyDrivers(lat, lng, dzongkhag));
    }

    // ── Trip path history ─────────────────────────────────────────────────────

    /**
     * GET /api/yaya/location/booking/{bookingId}/path
     * Returns ordered list of GPS points for a trip.
     * React frontend uses this to draw the route polyline.
     */
    @GetMapping("/booking/{bookingId}/path")
    public ResponseEntity<List<TripLocationHistory>> getTripPath(
            @PathVariable Long bookingId) {
        return ResponseEntity.ok(trackingService.getTripPath(bookingId));
    }

    /**
     * GET /api/yaya/location/booking/{bookingId}/distance
     * Total actual distance driven — used to verify fare for intra Pull trips.
     */
    @GetMapping("/booking/{bookingId}/distance")
    public ResponseEntity<Double> getActualDistance(@PathVariable Long bookingId) {
        return trackingService.getTripActualDistance(bookingId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
