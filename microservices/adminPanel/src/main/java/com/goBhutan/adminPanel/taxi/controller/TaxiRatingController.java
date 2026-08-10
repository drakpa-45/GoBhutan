package com.goBhutan.adminPanel.taxi.controller;

import com.goBhutan.adminPanel.taxi.dto.request.TaxiRatingRequest;
import com.goBhutan.adminPanel.taxi.dto.response.TaxiRatingResponse;
import com.goBhutan.adminPanel.taxi.service.TaxiRatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/taxi/ratings")
@RequiredArgsConstructor
public class TaxiRatingController {

    private final TaxiRatingService ratingService;

    /**
     * POST /taxi/ratings
     * Passenger submits a rating for a completed trip.
     *
     * {
     *   "bookingId": 42,
     *   "rating": 5,
     *   "comment": "Very smooth ride, driver was punctual."
     * }
     */
    @PostMapping
    public ResponseEntity<TaxiRatingResponse> submitRating(
            @Valid @RequestBody TaxiRatingRequest request) {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        String passengerId = jwt.getSubject();
        return ResponseEntity.ok(ratingService.submitRating(passengerId, request));
    }

    /**
     * GET /taxi/ratings/booking/{bookingId}
     * Get the rating for a specific booking.
     */
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<TaxiRatingResponse> getRatingByBooking(
            @PathVariable Long bookingId) {
        return ResponseEntity.ok(ratingService.getRatingByBooking(bookingId));
    }

    /**
     * GET /taxi/ratings/driver/{driverId}
     * Get all ratings for a driver with average score.
     * Used in nearby driver search to display star rating.
     */
    @GetMapping("/driver/{driverId}")
    public ResponseEntity<Map<String, Object>> getDriverRatings(
            @PathVariable String driverId) {
        return ResponseEntity.ok(ratingService.getDriverRatings(driverId));
    }
}