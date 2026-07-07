package com.goBhutan.adminPanel.taxi.controller;

import com.goBhutan.adminPanel.taxi.entity.InterRoute;
import com.goBhutan.adminPanel.taxi.repository.InterRouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/taxi/routes")
@RequiredArgsConstructor
public class InterRouteController {

    private final InterRouteRepository routeRepo;

    /**
     * Driver publishes a new inter-dzongkhag route.
     * POST /api/yaya/routes
     */
    @PostMapping
    public ResponseEntity<InterRoute> createRoute(@RequestBody InterRoute route) {
        route.setAvailableSeats(route.getTotalSeats());
        if (route.getSurgeMultiplier() == null)
            route.setSurgeMultiplier(BigDecimal.ONE);
        return ResponseEntity.ok(routeRepo.save(route));
    }

    /**
     * Get a routes by driver id.
     * GET /taxi/routes/getMyRoutes
     */
    @GetMapping("/getMyRoutes")
    public ResponseEntity<List<InterRoute>> getMyRoutes() {
        Jwt principal = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String driverId = principal.getSubject();
        return ResponseEntity.ok(routeRepo.findByDriverIdAndIsActiveTrue(driverId));
    }
    /**
     * Search routes by origin and destination dzongkhag.
     * GET /api/yaya/routes?origin=Thimphu&destination=Phuentsholing
     */
    @GetMapping
    public ResponseEntity<List<InterRoute>> searchRoutes(
            @RequestParam String origin,
            @RequestParam String destination) {
        return ResponseEntity.ok(
                routeRepo.findByOriginDzongkhagAndDestinationDzongkhagAndIsActiveTrue(
                        origin, destination));
    }

    /**
     * Get a single route with live seat count.
     * GET /api/yaya/routes/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<InterRoute> getRoute(@PathVariable Long id) {
        return routeRepo.findByIdAndIsActiveTrue(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Driver deactivates a route (soft delete).
     * PATCH /api/yaya/routes/{id}/deactivate
     */
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        routeRepo.findById(id).ifPresent(r -> {
            r.setIsActive(false);
            routeRepo.save(r);
        });
        return ResponseEntity.ok().build();
    }
}
