package com.goBhutan.adminPanel.busAdmin.controller;

import com.goBhutan.adminPanel.busAdmin.dto.BusRouteMasterRequest;
import com.goBhutan.adminPanel.busAdmin.dto.BusRouteMasterResponse;
import com.goBhutan.adminPanel.busAdmin.service.BusRouteMasterService;
import com.goBhutan.adminPanel.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/bus-masters")
@RequiredArgsConstructor
public class BusMastersController {

    private final BusRouteMasterService routeMasterService;

    @PostMapping("/routes")
    public ResponseEntity<ApiResponse<BusRouteMasterResponse>> createRouteMaster(
            @Valid @RequestBody BusRouteMasterRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Route master created successfully",
                routeMasterService.create(request, currentUserId())
        ));
    }

    @PutMapping("/routes/{id}")
    public ResponseEntity<ApiResponse<BusRouteMasterResponse>> updateRouteMaster(
            @PathVariable Long id,
            @Valid @RequestBody BusRouteMasterRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Route master updated successfully",
                routeMasterService.update(id, request, currentUserId())
        ));
    }

    @PatchMapping("/routes/{id}/disable")
    public ResponseEntity<ApiResponse<BusRouteMasterResponse>> disableRouteMaster(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Route master disabled successfully",
                routeMasterService.disable(id, currentUserId())
        ));
    }

    @GetMapping("/routes/{id}")
    public ResponseEntity<ApiResponse<BusRouteMasterResponse>> getRouteMaster(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                routeMasterService.getById(id)
        ));
    }

    @GetMapping("/routes")
    public ResponseEntity<ApiResponse<List<BusRouteMasterResponse>>> getRouteMasters(
            @RequestParam(name = "activeOnly", required = false) Boolean activeOnly
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                routeMasterService.getAll(activeOnly)
        ));
    }

    private String currentUserId() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return jwt.getSubject();
    }
}
