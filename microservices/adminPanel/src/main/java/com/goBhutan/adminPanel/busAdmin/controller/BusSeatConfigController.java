package com.goBhutan.adminPanel.busAdmin.controller;

import com.goBhutan.adminPanel.busAdmin.entity.BusSeatConfig;
import com.goBhutan.adminPanel.busAdmin.service.BusSeatConfigService;
import com.goBhutan.adminPanel.busAdmin.service.BusSeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/buses/{busId}/seat-configs")
public class BusSeatConfigController {
    @Autowired
    private  BusSeatConfigService seatConfigService;
    @Autowired
    private  BusSeatService seatService;

    @GetMapping
    public ResponseEntity<List<BusSeatConfig>> getConfigs(@PathVariable Long busId) {
        return ResponseEntity.ok(seatConfigService.getConfigsByBus(busId));
    }

    // API Example
    @PostMapping("/generate-seats")
    public ResponseEntity<?> generateLayout(@PathVariable Long busId,@RequestParam(defaultValue = "false") boolean forceRegenerate) {
        try {
            List<BusSeatConfig> seats = seatConfigService.generateSeatLayout(busId, forceRegenerate);
            return ResponseEntity.ok(seats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

   /* @PostMapping
    public ResponseEntity<?> addConfig(@PathVariable Long busId, @RequestBody BusSeatConfig config) {
        try {
            BusSeatConfig saved = seatConfigService.addConfig(busId, config);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{configId}")
    public ResponseEntity<?> updateConfig(@PathVariable Long configId, @RequestBody BusSeatConfig config) {
        try {
            BusSeatConfig updated = seatConfigService.updateConfig(configId, config);
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
    }*/

}