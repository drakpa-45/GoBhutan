package com.goBhutan.adminPanel.busAdmin.controller;

import com.goBhutan.adminPanel.busAdmin.entity.BusSeat;
import com.goBhutan.adminPanel.busAdmin.entity.SeatConfig;
import com.goBhutan.adminPanel.busAdmin.service.SeatConfigService;
import com.goBhutan.adminPanel.busAdmin.service.SeatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/buses/{busId}/seat-configs")
public class SeatConfigController {
    private final SeatConfigService seatConfigService;
    private final SeatService seatService;

    public SeatConfigController(SeatConfigService seatConfigService, SeatService seatService) {
        this.seatConfigService = seatConfigService;
        this.seatService = seatService;
    }

    @GetMapping
    public ResponseEntity<List<SeatConfig>> getConfigs(@PathVariable Long busId) {
        return ResponseEntity.ok(seatConfigService.getConfigsByBus(busId));
    }

    @PostMapping
    public ResponseEntity<?> addConfig(@PathVariable Long busId, @RequestBody SeatConfig config) {
        try {
            SeatConfig saved = seatConfigService.addConfig(busId, config);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{configId}")
    public ResponseEntity<?> updateConfig(@PathVariable Long configId, @RequestBody SeatConfig config) {
        try {
            SeatConfig updated = seatConfigService.updateConfig(configId, config);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{configId}")
    public ResponseEntity<Void> deleteConfig(@PathVariable Long configId) {
        seatConfigService.deleteConfig(configId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/generate-seats")
    public ResponseEntity<?> generateSeats(@PathVariable Long busId) {
        try {
            List<BusSeat> seats = seatService.generateSeatsForBus(busId);
            return ResponseEntity.ok(seats);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}