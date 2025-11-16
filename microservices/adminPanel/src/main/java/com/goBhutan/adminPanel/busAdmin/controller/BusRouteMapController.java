package com.goBhutan.adminPanel.busAdmin.controller;

import com.goBhutan.adminPanel.busAdmin.dto.BusRouteMapRequest;
import com.goBhutan.adminPanel.busAdmin.dto.BusRouteMapResponse;
import com.goBhutan.adminPanel.busAdmin.service.BusRouteMapService;
import com.goBhutan.adminPanel.common.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bus-route-map")
@CrossOrigin(origins = "*", maxAge = 3600)
public class BusRouteMapController {

    @Autowired
    private BusRouteMapService busRouteMapService;

    @PostMapping
    public ResponseEntity<ApiResponse<BusRouteMapResponse>> addMapping(@Valid @RequestBody BusRouteMapRequest req) {
        try {
            Jwt principal = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String adminUserId = principal.getSubject();
            BusRouteMapResponse res = busRouteMapService.addMapping(req, adminUserId);
            return ResponseEntity.ok(ApiResponse.success("Mapping added successfully", res));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BusRouteMapResponse>> updateMapping(@PathVariable Long id,
                                                                          @Valid @RequestBody BusRouteMapRequest req) {
        try {
            Jwt principal = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String adminUserId = principal.getSubject();
            BusRouteMapResponse res = busRouteMapService.updateMapping(id, req, adminUserId);
            return ResponseEntity.ok(ApiResponse.success("Mapping updated successfully", res));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/bus/{busId}")
    public ResponseEntity<ApiResponse<List<BusRouteMapResponse>>> getMappings(@PathVariable Long busId) {
        try {
            Jwt principal = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String adminUserId = principal.getSubject();
            List<BusRouteMapResponse> list = busRouteMapService.getMappingsByBus(busId, adminUserId);
            return ResponseEntity.ok(ApiResponse.success(list));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteMapping(@PathVariable Long id) {
        try {
            Jwt principal = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String adminUserId = principal.getSubject();
            busRouteMapService.deleteMapping(id, adminUserId);
            return ResponseEntity.ok(ApiResponse.success("Mapping deleted successfully", "Deleted"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
