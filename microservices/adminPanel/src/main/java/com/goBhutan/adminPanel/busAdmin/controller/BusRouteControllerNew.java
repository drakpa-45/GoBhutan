package com.goBhutan.adminPanel.busAdmin.controller;

import com.goBhutan.adminPanel.busAdmin.dto.BusRouteRequest;
import com.goBhutan.adminPanel.busAdmin.dto.BusRouteResponse;
import com.goBhutan.adminPanel.busAdmin.service.BusRouteServiceNew;
import com.goBhutan.adminPanel.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/bus-routes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class BusRouteControllerNew {
    @Autowired
    private BusRouteServiceNew busRouteService;

    @PostMapping
    public ResponseEntity<ApiResponse<BusRouteResponse>> create(@Valid @RequestBody BusRouteRequest request) {

        Jwt principal = (Jwt) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        String adminUserId = principal.getSubject();

        BusRouteResponse response = busRouteService.createRoute(request, adminUserId);

        return ResponseEntity.ok(ApiResponse.success("Bus route created successfully", response));
    }

    // ----------------------- UPDATE -----------------------
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BusRouteResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody BusRouteRequest request) {

        Jwt principal = (Jwt) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        String adminUserId = principal.getSubject();

        BusRouteResponse response = busRouteService.updateRoute(id, request, adminUserId);

        return ResponseEntity.ok(ApiResponse.success("Bus route updated successfully", response));
    }

    // ----------------------- SOFT DELETE -----------------------
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> softDelete(@PathVariable Long id) {

        Jwt principal = (Jwt) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        String adminUserId = principal.getSubject();

        busRouteService.softDeleteRoute(id, adminUserId);

        return ResponseEntity.ok(ApiResponse.success("Bus route deleted successfully", "DELETED"));
    }

    // ----------------------- APP USER SEARCH -----------------------
    @GetMapping("/active/search")
    public ResponseEntity<ApiResponse<List<BusRouteResponse>>> searchActiveRoutesByRoute(
            @RequestParam String source,
            @RequestParam String destination,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            List<BusRouteResponse> routes = busRouteService
                    .getActiveRoutesBySourceAndDestination(source, destination, date);
            return ResponseEntity.ok(ApiResponse.success(routes));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // ----------------------- GET ONE -----------------------
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BusRouteResponse>> getOne(@PathVariable Long id) {

        Jwt principal = (Jwt) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        String adminUserId = principal.getSubject();

        BusRouteResponse response = busRouteService.getRoute(id, adminUserId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ----------------------- GET ALL BY BUS -----------------------
    @GetMapping("/bus/{busId}")
    public ResponseEntity<ApiResponse<List<BusRouteResponse>>> getByBus(@PathVariable Long busId) {

        Jwt principal = (Jwt) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        String adminUserId = principal.getSubject();

        List<BusRouteResponse> routes = busRouteService.getRoutesByBus(busId, adminUserId);

        return ResponseEntity.ok(ApiResponse.success(routes));
    }
}
