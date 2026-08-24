package com.goBhutan.adminPanel.gasDelivery.controller;

import com.goBhutan.adminPanel.common.dto.ApiResponse;
import com.goBhutan.adminPanel.gasDelivery.dto.GasDeliveryAdminStatusRequest;
import com.goBhutan.adminPanel.gasDelivery.dto.GasDeliveryCreateRequest;
import com.goBhutan.adminPanel.gasDelivery.dto.GasDeliveryResponse;
import com.goBhutan.adminPanel.gasDelivery.service.GasDeliveryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gas-delivery")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class GasDeliveryController {

    private final GasDeliveryService gasDeliveryService;

    @PostMapping("/")
    public ResponseEntity<ApiResponse<GasDeliveryResponse>> createGasDelivery(
            @Valid @RequestBody GasDeliveryCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Gas delivery request created successfully",
                gasDeliveryService.create(request, currentUserId())));
    }

    @GetMapping({ "/" })
    public ResponseEntity<ApiResponse<List<GasDeliveryResponse>>> getGasDeliveries() {
        return ResponseEntity.ok(ApiResponse.success(
                gasDeliveryService.getAll()));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<GasDeliveryResponse>> updateGasDeliveryStatus(
            @PathVariable Long id,
            @Valid @RequestBody GasDeliveryAdminStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Gas delivery status updated successfully",
                gasDeliveryService.updateStatus(id, request, currentUserId())));
    }

    private String currentUserId() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return jwt.getSubject();
    }
}
