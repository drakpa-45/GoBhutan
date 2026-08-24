package com.goBhutan.adminPanel.busAdmin.controller;

import com.goBhutan.adminPanel.busAdmin.dto.SeatDetailsDTO;
import com.goBhutan.adminPanel.busAdmin.entity.BusSeatConfig;
import com.goBhutan.adminPanel.busAdmin.service.BusSeatConfigService;
import com.goBhutan.adminPanel.common.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/buses/{busId}/seat-configs")
public class BusSeatConfigController {
    @Autowired
    private  BusSeatConfigService seatConfigService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SeatDetailsDTO>>> getConfigs(@PathVariable Long busId) {
        try {
            String adminUserId = currentUserId();
            List<SeatDetailsDTO> seats = seatConfigService.getConfigsByBus(busId, adminUserId)
                    .stream()
                    .map(this::toSeatDetails)
                    .toList();

            return ResponseEntity.ok(ApiResponse.success(seats));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/generate-seats")
    public ResponseEntity<ApiResponse<List<SeatDetailsDTO>>> generateLayout(
            @PathVariable Long busId,
            @RequestParam(defaultValue = "false") boolean forceRegenerate) {
        try {
            String adminUserId = currentUserId();

            List<BusSeatConfig> seats = seatConfigService.generateSeatLayout(busId, forceRegenerate, adminUserId);
            List<SeatDetailsDTO> response = seats.stream()
                    .map(this::toSeatDetails)
                    .toList();
            return ResponseEntity.ok(ApiResponse.success("Seat layout generated successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    private String currentUserId() {
        Jwt principal = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return principal.getSubject();
    }

    private SeatDetailsDTO toSeatDetails(BusSeatConfig seat) {
        return new SeatDetailsDTO(
                seat.getStartNo(),
                seat.getSeatLabel(),
                seat.getSeatType(),
                null
        );
    }

}
