package com.goBhutan.adminPanel.taxi.service;


import com.goBhutan.adminPanel.taxi.dto.request.LocationPingRequest;
import com.goBhutan.adminPanel.taxi.dto.response.DriverPositionResponse;
import com.goBhutan.adminPanel.taxi.dto.response.NearbyDriverResponse;
import com.goBhutan.adminPanel.taxi.dto.response.VehicleImageResponse;
import com.goBhutan.adminPanel.taxi.entity.DriverLocation;
import com.goBhutan.adminPanel.taxi.entity.TaxiDriver;
import com.goBhutan.adminPanel.taxi.entity.TripLocationHistory;
import com.goBhutan.adminPanel.taxi.repository.DriverLocationRepository;
import com.goBhutan.adminPanel.taxi.repository.TaxiDriverImageRepository;
import com.goBhutan.adminPanel.taxi.repository.TaxiDriverRepository;
import com.goBhutan.adminPanel.taxi.repository.TripLocationHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaxiLocationTrackingService {

    private final DriverLocationRepository driverLocationRepo;
    private final TripLocationHistoryRepository historyRepo;
    private final TaxiDriverRepository taxiDriverRepo;       // add this
    private final TaxiDriverImageRepository taxiDriverImageRepo;  // add this


    /** Driver considered offline if not pinged within this many seconds */
    private static final long OFFLINE_THRESHOLD_SECONDS = 120;

    /** Search radius for Pull mode nearest-driver matching (km) */
    private static final double SEARCH_RADIUS_KM = 5.0;

    // ─────────────────────────────────────────────────────────────────────────
    // DRIVER → SERVER  (every 30 seconds from driver app)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Main entry point for every location ping from the driver app.
     *
     * Does two things:
     *  1. Upserts tbl_yaya_driver_location (latest position, always one row per driver)
     *  2. If driver is on an active trip → appends to tbl_yaya_trip_location_history
     */
    @Transactional
    public DriverPositionResponse handlePing(LocationPingRequest req) {

        // 1. Upsert latest driver position
        DriverLocation location = driverLocationRepo
                .findByDriverId(req.getDriverId())
                .orElse(DriverLocation.builder()
                        .driverId(req.getDriverId())
                        .build());

        location.setLatitude(req.getLatitude());
        location.setLongitude(req.getLongitude());
        location.setBearing(req.getBearing());
        location.setSpeedKmh(req.getSpeedKmh());
        location.setCurrentBookingId(req.getCurrentBookingId());

        if (req.getIsOnline() != null) location.setIsOnline(req.getIsOnline());
        if (location.getIsOnline() == null) location.setIsOnline(false);

        driverLocationRepo.save(location);
        log.debug("Location ping from driver {} at ({}, {})",
                req.getDriverId(), req.getLatitude(), req.getLongitude());

        // 2. Append to trip history if mid-trip
        if (req.getCurrentBookingId() != null) {
            appendTripHistory(req);
        }

        return toPositionResponse(location);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PASSENGER → SERVER  (passenger app polling or WebSocket subscription)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns the current position of the driver assigned to a booking.
     * Passenger app calls this every 30 seconds to animate driver on map.
     */
    public Optional<DriverPositionResponse> getDriverForBooking(Long bookingId) {
        return driverLocationRepo.findByCurrentBookingId(bookingId)
                .stream()
                .findFirst()
                .map(this::toPositionResponse);
    }

    /**
     * Find nearby available drivers for Pull mode.
     * Returns up to 5 closest drivers sorted by straight-line distance.
     *
     * @param passengerLat  passenger pickup latitude
     * @param passengerLng  passenger pickup longitude
     * @param dzongkhag     non-null = intra filter; null = any (inter pull)
     */
    public List<NearbyDriverResponse> findNearbyDrivers(
            BigDecimal passengerLat,
            BigDecimal passengerLng,
            String dzongkhag) {

        double lat = passengerLat.doubleValue();
        double lng = passengerLng.doubleValue();
        double[] box = HaversineUtil.boundingBox(lat, lng, SEARCH_RADIUS_KM);
        LocalDateTime cutoff = LocalDateTime.now().minusSeconds(OFFLINE_THRESHOLD_SECONDS);

        BigDecimal latMin = BigDecimal.valueOf(box[0]);
        BigDecimal latMax = BigDecimal.valueOf(box[1]);
        BigDecimal lngMin = BigDecimal.valueOf(box[2]);
        BigDecimal lngMax = BigDecimal.valueOf(box[3]);

        List<DriverLocation> candidates = (dzongkhag != null)
                ? driverLocationRepo.findAvailableInDzongkhag(
                dzongkhag, latMin, latMax, lngMin, lngMax, cutoff)
                : driverLocationRepo.findAvailableInBoundingBox(
                latMin, latMax, lngMin, lngMax, cutoff);

        return candidates.stream()
                .map(d -> {
                    double dist = HaversineUtil.distanceKm(
                            d.getLatitude(), d.getLongitude(),
                            passengerLat, passengerLng);

                    // Fetch driver + taxi details
                    TaxiDriver taxi = taxiDriverRepo
                            .findByDriverId(d.getDriverId())
                            .orElse(null);

                    List<VehicleImageResponse> images = taxi != null
                            ? taxiDriverImageRepo
                            .findByTaxiDriverIdOrderByDisplayOrderAsc(taxi.getId())
                            .stream()
                            .map(img -> VehicleImageResponse.builder()
                                    .id(img.getId())
                                    .imagePath(img.getImagePath())
                                    .originalFilename(img.getOriginalFilename())
                                    .displayOrder(img.getDisplayOrder())
                                    .uploadedAt(img.getUploadedAt())
                                    .build())
                            .collect(Collectors.toList())
                            : Collections.emptyList();

                    return NearbyDriverResponse.builder()
                            .driverId(d.getDriverId())
                            .latitude(d.getLatitude())
                            .longitude(d.getLongitude())
                            .bearing(d.getBearing())
                            .distanceKm(BigDecimal.valueOf(dist)
                                    .setScale(2, RoundingMode.HALF_UP))
                            .etaMinutes(HaversineUtil.etaMinutes(dist))
                            .currentDzongkhag(d.getCurrentDzongkhag())
                            // Driver details
                            .driverName(taxi != null ? taxi.getDriverName() : null)
                            .contactNumber(taxi != null ? taxi.getPhoneNumber() : null)
                            // Taxi details
                            .vehicleMake(taxi != null ? taxi.getVehicleMake() : null)
                            .vehicleModel(taxi != null ? taxi.getVehicleModel() : null)
                            .vehicleColor(taxi != null ? taxi.getVehicleColor() : null)
                            .totalSeats(taxi != null ? taxi.getTotalSeats() : null)
                            .registrationNumber(taxi != null ? taxi.getRegistrationNumber() : null)
                            .images(images)
                            .build();
                })
                .filter(r -> r.getDistanceKm().doubleValue() <= SEARCH_RADIUS_KM)
                .sorted(Comparator.comparingDouble(r -> r.getDistanceKm().doubleValue()))
                .limit(5)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TRIP PATH HISTORY
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns the full ordered path of a completed or in-progress trip.
     * React frontend uses this to draw the polyline on the map.
     */
    public List<TripLocationHistory> getTripPath(Long bookingId) {
        return historyRepo.findByBookingIdOrderByRecordedAtAsc(bookingId);
    }

    /**
     * Returns the total distance driven for a trip (km).
     * Used to cross-check fare for intra-dzongkhag Pull trips.
     */
    public Optional<Double> getTripActualDistance(Long bookingId) {
        return historyRepo.findTotalDistanceByBookingId(bookingId);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ONLINE / OFFLINE MANAGEMENT
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public void setOnlineStatus(String driverId, boolean online) {
        driverLocationRepo.findByDriverId(driverId).ifPresent(loc -> {
            loc.setIsOnline(online);
            if (!online) loc.setCurrentBookingId(null);
            driverLocationRepo.save(loc);
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private void appendTripHistory(LocationPingRequest req) {
        // Compute incremental distance from last recorded point
        Optional<TripLocationHistory> lastPoint =
                historyRepo.findTopByBookingIdOrderByRecordedAtDesc(req.getCurrentBookingId());

        BigDecimal cumulativeDist = BigDecimal.ZERO;

        if (lastPoint.isPresent()) {
            TripLocationHistory prev = lastPoint.get();
            double segmentKm = HaversineUtil.distanceKm(
                    prev.getLatitude(), prev.getLongitude(),
                    req.getLatitude(), req.getLongitude());
            cumulativeDist = prev.getCumulativeDistanceKm()
                    .add(BigDecimal.valueOf(segmentKm)
                            .setScale(3, RoundingMode.HALF_UP));
        }

        TripLocationHistory point = TripLocationHistory.builder()
                .bookingId(req.getCurrentBookingId())
                .driverId(req.getDriverId())
                .latitude(req.getLatitude())
                .longitude(req.getLongitude())
                .bearing(req.getBearing())
                .speedKmh(req.getSpeedKmh())
                .cumulativeDistanceKm(cumulativeDist)
                .recordedAt(LocalDateTime.now())
                .build();

        historyRepo.save(point);
        log.debug("Trip history point saved for booking {} — cumulative {:.2f} km",
                req.getCurrentBookingId(), cumulativeDist);
    }

    private DriverPositionResponse toPositionResponse(DriverLocation loc) {
        long secondsAgo = loc.getLastUpdatedAt() != null
                ? ChronoUnit.SECONDS.between(loc.getLastUpdatedAt(), LocalDateTime.now())
                : 999L;
        return DriverPositionResponse.builder()
                .driverId(loc.getDriverId())
                .latitude(loc.getLatitude())
                .longitude(loc.getLongitude())
                .bearing(loc.getBearing())
                .speedKmh(loc.getSpeedKmh())
                .isOnline(loc.getIsOnline())
                .currentBookingId(loc.getCurrentBookingId())
                .currentDzongkhag(loc.getCurrentDzongkhag())
                .lastUpdatedAt(loc.getLastUpdatedAt())
                .secondsSinceLastPing(secondsAgo)
                .build();
    }
}
