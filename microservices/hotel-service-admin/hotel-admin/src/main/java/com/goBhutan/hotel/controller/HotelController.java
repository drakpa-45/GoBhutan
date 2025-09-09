package com.goBhutan.hotel.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.goBhutan.hotel.entity.Hotel;
import com.goBhutan.hotel.service.AmenityService;
import com.goBhutan.hotel.service.HotelService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/hotels")
public class HotelController {

    @Autowired
    private HotelService hotelService;

    @Autowired
    private AmenityService amenityService;

    // List hotels for current admin
    @GetMapping
    public ResponseEntity<List<Hotel>> listHotels(@AuthenticationPrincipal Jwt jwt) {
        String adminUserId = jwt.getSubject();
        List<Hotel> hotels = hotelService.getHotelsByAdmin(adminUserId);
        return ResponseEntity.ok(hotels);
    }

    // View a specific hotel
    @GetMapping("/{id}")
    public ResponseEntity<Hotel> viewHotel(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        String adminUserId = jwt.getSubject();
        return hotelService.getHotelById(id, adminUserId)
                .map(hotel -> ResponseEntity.ok(hotel))
                .orElse(ResponseEntity.notFound().build());
    }

    // Register / create a new hotel
    @PostMapping("/register")
    @PreAuthorize("hasRole('client_admin')")
    public ResponseEntity<?> registerHotel(@Valid @RequestBody Hotel hotel,
                                           BindingResult bindingResult,
                                           @RequestParam(value = "amenityIds", required = false) List<Long> amenityIds,
                                           @AuthenticationPrincipal Jwt jwt) {
        if (bindingResult.hasErrors()) {
            // Return validation errors as JSON
            List<String> errors = bindingResult.getFieldErrors()
                    .stream()
                    .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(errors);
        }

        String adminUserId = jwt.getSubject();
        hotel.setAdminUserId(adminUserId);

        if (amenityIds != null && !amenityIds.isEmpty()) {
            var amenities = amenityIds.stream()
                    .map(id -> amenityService.getAllAmenities().stream()
                            .filter(a -> a.getId().equals(id))
                            .findFirst().orElse(null))
                    .filter(a -> a != null)
                    .collect(Collectors.toList());
            hotel.setAmenities(amenities);
        }

        Hotel saved = hotelService.saveHotel(hotel, adminUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // Delete a hotel
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('client_admin')")
    public ResponseEntity<?> deleteHotel(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        String adminUserId = jwt.getSubject();
        try {
            hotelService.deleteHotel(id, adminUserId);
            return ResponseEntity.ok(Map.of("message", "Hotel deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body(Map.of("message", "Hotel not found"));
        }
    }
}
