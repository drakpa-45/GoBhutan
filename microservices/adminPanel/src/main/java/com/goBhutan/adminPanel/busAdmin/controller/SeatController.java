package com.goBhutan.adminPanel.busAdmin.controller;

import com.goBhutan.adminPanel.busAdmin.entity.BusSeat;
import com.goBhutan.adminPanel.busAdmin.service.SeatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/buses/{busId}/seats")
public class SeatController {

    private final SeatService seatService;

    public SeatController(SeatService seatService) {
        this.seatService = seatService;
    }

    @GetMapping
    public ResponseEntity<List<BusSeat>> getSeats(@PathVariable Long busId) {
        return ResponseEntity.ok(seatService.getSeatsByBus(busId));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAllSeats(@PathVariable Long busId) {
        seatService.deleteAllSeatsByBus(busId);
        return ResponseEntity.noContent().build();
    }
}
