package com.goBhutan.adminPanel.busAdmin.controller;

import com.goBhutan.adminPanel.busAdmin.dto.BusRouteRequest;
import com.goBhutan.adminPanel.busAdmin.dto.BusRouteResponse;
import com.goBhutan.adminPanel.busAdmin.service.BusRouteServiceNew;
import com.goBhutan.adminPanel.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

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
