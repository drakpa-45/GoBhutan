package com.goBhutan.adminPanel.gasDelivery.controller;

import com.goBhutan.adminPanel.common.dto.ApiResponse;
import com.goBhutan.adminPanel.gasDelivery.dto.GasConfigMasterRequest;
import com.goBhutan.adminPanel.gasDelivery.dto.GasConfigMasterResponse;
import com.goBhutan.adminPanel.gasDelivery.service.GasConfigMasterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gas-config")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class GasConfigMasterController {

        private final GasConfigMasterService gasConfigMasterService;

        @PostMapping("/")
        public ResponseEntity<ApiResponse<GasConfigMasterResponse>> createGasConfigMaster(
                        @Valid @RequestBody GasConfigMasterRequest request) {
                return ResponseEntity.ok(ApiResponse.success(
                                "Gas Config master created successfully",
                                gasConfigMasterService.create(request, currentUserId())));
        }

        @PutMapping("/{id}")
        public ResponseEntity<ApiResponse<GasConfigMasterResponse>> updateGasConfigMaster(
                        @PathVariable Long id,
                        @Valid @RequestBody GasConfigMasterRequest request) {
                return ResponseEntity.ok(ApiResponse.success(
                                "Gas Config master updated successfully",
                                gasConfigMasterService.update(id, request, currentUserId())));
        }

        @PatchMapping("/{id}/disable")
        public ResponseEntity<ApiResponse<GasConfigMasterResponse>> disableGasConfigMaster(@PathVariable Long id) {
                return ResponseEntity.ok(ApiResponse.success(
                                "Gas Config master disabled successfully",
                                gasConfigMasterService.disable(id, currentUserId())));
        }

        @GetMapping("/{id}")
        public ResponseEntity<ApiResponse<GasConfigMasterResponse>> getGasConfigMaster(@PathVariable Long id) {
                return ResponseEntity.ok(ApiResponse.success(
                                gasConfigMasterService.getById(id)));
        }

        @GetMapping("/")
        public ResponseEntity<ApiResponse<List<GasConfigMasterResponse>>> getGasConfigMaster(
                        @RequestParam(name = "activeOnly", required = false) Boolean activeOnly) {
                return ResponseEntity.ok(ApiResponse.success(
                                gasConfigMasterService.getAll(activeOnly)));
        }

        private String currentUserId() {
                Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                return jwt.getSubject();
        }
}
