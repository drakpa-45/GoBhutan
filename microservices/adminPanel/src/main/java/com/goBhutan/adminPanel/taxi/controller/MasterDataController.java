package com.goBhutan.adminPanel.taxi.controller;

import com.goBhutan.adminPanel.taxi.entity.DropPoint;
import com.goBhutan.adminPanel.taxi.entity.Dzongkhag;
import com.goBhutan.adminPanel.taxi.repository.DzongkhagRepository;
import com.goBhutan.adminPanel.taxi.service.DropPointService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/taxi/master")
@RequiredArgsConstructor
public class MasterDataController {

    private final DzongkhagRepository dzongkhagRepo;
    private final DropPointService dropPointService;

    /** All dzongkhags — for origin/destination dropdown */
    @GetMapping("/dzongkhags")
    public ResponseEntity<List<Dzongkhag>> getAllDzongkhags() {
        return ResponseEntity.ok(dzongkhagRepo.findAllByOrderByNameAsc());
    }

    /**
     * Drop points within corridor between origin and destination.
     * Driver calls this after selecting origin + destination.
     *
     * GET /taxi/master/drop-points?originId=1&destinationId=5&bufferKm=30
     */
    @GetMapping("/drop-points")
    public ResponseEntity<List<DropPoint>> getDropPoints(
            @RequestParam Long originId,
            @RequestParam Long destinationId,
            @RequestParam(defaultValue = "30") double bufferKm) {
        return ResponseEntity.ok(
                dropPointService.getDropPointsInCorridor(originId, destinationId, bufferKm));
    }
}