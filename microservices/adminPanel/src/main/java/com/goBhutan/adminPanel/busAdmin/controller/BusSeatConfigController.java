package com.goBhutan.adminPanel.busAdmin.controller;

import com.goBhutan.adminPanel.busAdmin.dto.SeatDetailsDTO;
import com.goBhutan.adminPanel.busAdmin.entity.BusSeatConfig;
import com.goBhutan.adminPanel.busAdmin.service.BusSeatConfigService;
import com.goBhutan.adminPanel.common.dto.ApiResponse;
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

//    @GetMapping
//    public ResponseEntity<ApiResponse<List<BusSeatConfig>>> getConfigs(@PathVariable Long busId) {
//        List<BusSeatConfig> seats = seatConfigService.getConfigsByBus(busId);
//        return ResponseEntity.ok(ApiResponse.success(seats));
//        //return ResponseEntity.ok(seatConfigService.getConfigsByBus(busId));
//    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SeatDetailsDTO>>> getConfigs(@PathVariable Long busId) {

        List<SeatDetailsDTO> seats = seatConfigService.getConfigsByBus(busId)
                .stream()
                .map(s -> new SeatDetailsDTO(s.getStartNo(),
                        s.getSeatLabel(),
                        s.getSeatType(),
                        null ))
                .toList();

        return ResponseEntity.ok(ApiResponse.success(seats));
    }

    @PostMapping("/generate-seats")
    public ResponseEntity<?> generateLayout(@PathVariable Long busId,@RequestParam(defaultValue = "false") boolean forceRegenerate) {
        try {
            List<BusSeatConfig> seats = seatConfigService.generateSeatLayout(busId, forceRegenerate);
            return ResponseEntity.ok(seats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

}