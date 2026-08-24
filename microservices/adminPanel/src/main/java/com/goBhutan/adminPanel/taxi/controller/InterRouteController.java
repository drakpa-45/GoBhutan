package com.goBhutan.adminPanel.taxi.controller;

import com.goBhutan.adminPanel.taxi.dto.request.InterRouteRequest;
import com.goBhutan.adminPanel.taxi.dto.response.RouteSearchResponse;
import com.goBhutan.adminPanel.taxi.entity.DropPoint;
import com.goBhutan.adminPanel.taxi.entity.Dzongkhag;
import com.goBhutan.adminPanel.taxi.entity.InterRoute;
import com.goBhutan.adminPanel.taxi.entity.RouteStop;
import com.goBhutan.adminPanel.taxi.dto.request.RouteStopRequest;
import com.goBhutan.adminPanel.taxi.repository.DropPointRepository;
import com.goBhutan.adminPanel.taxi.repository.DzongkhagRepository;
import com.goBhutan.adminPanel.taxi.repository.InterRouteRepository;
import com.goBhutan.adminPanel.taxi.service.HaversineUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/taxi/routes")
@RequiredArgsConstructor
public class InterRouteController {

    private final InterRouteRepository routeRepo;
    private final DzongkhagRepository dzongkhagRepo;
    private final DropPointRepository dropPointRepo;

    /**
     * Driver publishes a new inter-dzongkhag route with stops.
     * POST /taxi/routes
     */
    @PostMapping
    public ResponseEntity<InterRoute> createRoute(
            @Valid @RequestBody InterRouteRequest req) {

        Jwt principal = (Jwt) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        String driverId = principal.getSubject();

        // Load origin dzongkhag coordinates for distance calculation
        Dzongkhag origin = dzongkhagRepo.findById(req.getOriginDzongkhagId())
                .orElseThrow(() -> new IllegalArgumentException("Origin dzongkhag not found"));

        Dzongkhag destination = dzongkhagRepo.findById(req.getDestinationDzongkhagId())
                .orElseThrow(() -> new IllegalArgumentException("Destination dzongkhag not found"));

        // Auto-calculate total route distance from coordinates
        BigDecimal routeDistanceKm = BigDecimal.valueOf(
                        HaversineUtil.distanceKm(
                                origin.getLatitude(), origin.getLongitude(),
                                destination.getLatitude(), destination.getLongitude()))
                .setScale(2, RoundingMode.HALF_UP);

        List<RouteStop> stops = new ArrayList<>();

        // Stop 0 — origin
        stops.add(RouteStop.builder()
                .stopSequence(0)
                .stopName(origin.getName())
                .dzongkhag(origin.getName())
                .distanceFromOriginKm(BigDecimal.ZERO)
                .etaMinutes(0)
                .build());

        // Intermediate stops — auto-calculate distance from origin
        if (req.getIntermediateStops() != null) {
            for (int i = 0; i < req.getIntermediateStops().size(); i++) {
                RouteStopRequest s = req.getIntermediateStops().get(i);

                DropPoint dp = dropPointRepo.findById(s.getDropPointId())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Drop point not found: " + s.getDropPointId()));

                // Auto-calculate distance from origin to this stop
                BigDecimal distFromOrigin = BigDecimal.valueOf(
                                HaversineUtil.distanceKm(
                                        origin.getLatitude(), origin.getLongitude(),
                                        dp.getLatitude(), dp.getLongitude()))
                        .setScale(2, RoundingMode.HALF_UP);

                // Auto-calculate ETA based on 30 km/h average
                int etaMinutes = HaversineUtil.etaMinutes(distFromOrigin.doubleValue());

                stops.add(RouteStop.builder()
                        .stopSequence(i + 1)
                        .stopName(dp.getName())
                        .dzongkhag(dp.getDzongkhag())
                        .distanceFromOriginKm(distFromOrigin)   // auto-calculated
                        .etaMinutes(etaMinutes)                  // auto-calculated
                        .dropPointId(dp.getId())
                        .build());
            }
        }

        // Last stop — destination
        stops.add(RouteStop.builder()
                .stopSequence(stops.size())
                .stopName(destination.getName())
                .dzongkhag(destination.getName())
                .distanceFromOriginKm(routeDistanceKm)
                .etaMinutes(HaversineUtil.etaMinutes(routeDistanceKm.doubleValue()))
                .build());

        InterRoute route = InterRoute.builder()
                .driverId(driverId)
                .originDzongkhag(origin.getName())
                .originAddress(origin.getName())
                .destinationDzongkhag(destination.getName())
                .destinationAddress(destination.getName())
                .routeDistanceKm(routeDistanceKm)              // auto-calculated
                .ratePerKmPerSeat(req.getRatePerKmPerSeat())
                .totalSeats(req.getTotalSeats())
                .availableSeats(req.getTotalSeats())
                .departureTime(req.getDepartureTime())
                .surgeMultiplier(BigDecimal.ONE)
                .isActive(true)
                .build();

        stops.forEach(s -> s.setRoute(route));
        route.setStops(stops);

        return ResponseEntity.ok(routeRepo.save(route));
    }

    /**
     * Get routes by driver id.
     * GET /taxi/routes/getMyRoutes
     */
    @GetMapping("/getMyRoutes")
    public ResponseEntity<List<InterRoute>> getMyRoutes() {
        Jwt principal = (Jwt) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        String driverId = principal.getSubject();
        return ResponseEntity.ok(routeRepo.findByDriverIdAndIsActiveTrue(driverId));
    }

    /**
     * GET /taxi/routes?origin=Thimphu&destination=Sarpang
     *
     * Searches routes where origin and destination match
     * ANY stop on the route — not just start and end.
     *
     * Example: Passenger at Chuzom wants to go to Sarpang.
     * Returns Thimphu→Samtse route because both are stops on it.
     */
    @GetMapping
    public ResponseEntity<List<RouteSearchResponse>> searchRoutes(
            @RequestParam String origin,
            @RequestParam String destination) {
        List<InterRoute> routes = routeRepo.searchByAnyStopToAnyStop(origin, destination);
        List<RouteSearchResponse> response = routes.stream()
                .map(r -> toSearchResponse(r, origin, destination))
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * Get a single route with live seat count and stops.
     * GET /taxi/routes/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<InterRoute> getRoute(@PathVariable Long id) {
        return routeRepo.findByIdAndIsActiveTrue(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Driver deactivates a route (soft delete).
     * PATCH /taxi/routes/{id}/deactivate
     */
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        routeRepo.findById(id).ifPresent(r -> {
            r.setIsActive(false);
            routeRepo.save(r);
        });
        return ResponseEntity.ok().build();
    }

    private RouteSearchResponse toSearchResponse(InterRoute route,
                                                 String requestedOrigin,
                                                 String requestedDestination) {
        List<RouteStop> allStops = route.getStops();

        // Find boarding stop — first stop matching requested origin
        RouteStop boardingStop = allStops.stream()
                .filter(s -> s.getStopName().equalsIgnoreCase(requestedOrigin)
                        || (s.getDzongkhag() != null
                        && s.getDzongkhag().equalsIgnoreCase(requestedOrigin)))
                .findFirst()
                .orElse(allStops.get(0));   // fallback to route origin

        // Find alighting stop — first stop matching requested destination
        RouteStop alightingStop = allStops.stream()
                .filter(s -> s.getStopName().equalsIgnoreCase(requestedDestination)
                        || (s.getDzongkhag() != null
                        && s.getDzongkhag().equalsIgnoreCase(requestedDestination)))
                .findFirst()
                .orElse(allStops.get(allStops.size() - 1));  // fallback to destination

        // Segment distance for this passenger
        BigDecimal segmentDistance = alightingStop.getDistanceFromOriginKm()
                .subtract(boardingStop.getDistanceFromOriginKm());

        // Pre-calculate estimated fare for display
        BigDecimal estimatedFare = segmentDistance
                .multiply(route.getRatePerKmPerSeat())
                .multiply(route.getSurgeMultiplier())
                .setScale(2, RoundingMode.HALF_UP);

        // Map all stops to detail
        List<RouteSearchResponse.StopDetail> stopDetails = allStops.stream()
                .map(s -> RouteSearchResponse.StopDetail.builder()
                        .stopId(s.getId())
                        .stopName(s.getStopName())
                        .dzongkhag(s.getDzongkhag())
                        .distanceFromOriginKm(s.getDistanceFromOriginKm())
                        .etaMinutes(s.getEtaMinutes())
                        .stopSequence(s.getStopSequence())
                        .build())
                .collect(Collectors.toList());

        return RouteSearchResponse.builder()
                .routeId(route.getId())
                .driverId(route.getDriverId())
                .departureTime(route.getDepartureTime())
                .availableSeats(route.getAvailableSeats())
                .ratePerKmPerSeat(route.getRatePerKmPerSeat())
                .fullRouteOrigin(route.getOriginDzongkhag())
                .fullRouteDestination(route.getDestinationDzongkhag())
                .fullRouteDistanceKm(route.getRouteDistanceKm())
                .boardingStop(RouteSearchResponse.StopDetail.builder()
                        .stopId(boardingStop.getId())
                        .stopName(boardingStop.getStopName())
                        .dzongkhag(boardingStop.getDzongkhag())
                        .distanceFromOriginKm(boardingStop.getDistanceFromOriginKm())
                        .etaMinutes(boardingStop.getEtaMinutes())
                        .stopSequence(boardingStop.getStopSequence())
                        .build())
                .alightingStop(RouteSearchResponse.StopDetail.builder()
                        .stopId(alightingStop.getId())
                        .stopName(alightingStop.getStopName())
                        .dzongkhag(alightingStop.getDzongkhag())
                        .distanceFromOriginKm(alightingStop.getDistanceFromOriginKm())
                        .etaMinutes(alightingStop.getEtaMinutes())
                        .stopSequence(alightingStop.getStopSequence())
                        .build())
                .segmentDistanceKm(segmentDistance)
                .estimatedFarePerSeat(estimatedFare)
                .allStops(stopDetails)
                .build();
    }
}