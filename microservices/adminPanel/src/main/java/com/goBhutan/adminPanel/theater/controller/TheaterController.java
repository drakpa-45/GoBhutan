package com.goBhutan.adminPanel.theater.controller;

import com.goBhutan.adminPanel.theater.dto.*;
import com.goBhutan.adminPanel.theater.service.TheaterService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/admin/theaters")
@PreAuthorize("hasRole('ADMIN') or hasRole('THEATER_OWNER')")
public class TheaterController {

    private final TheaterService theaterService;

    public TheaterController(TheaterService theaterService) {
        this.theaterService = theaterService;
    }

    /**
     * Get all theaters with optional filtering
     * Admin: sees all theaters
     * Theater Owner: sees only their theaters
     */
    @GetMapping
    public ResponseEntity<Page<TheaterDTO>> getAllTheaters(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String locationId,
            @RequestParam(required = false) String dzongkhag,
            @RequestParam(required = false) String search,
            Authentication authentication) {

        Pageable pageable = PageRequest.of(page, size);
        Page<TheaterDTO> theaters;

        // If user has THEATER_OWNER role (and not ADMIN), only show their theaters
        if (hasRole(authentication, "THEATER_OWNER") && !hasRole(authentication, "ADMIN")) {
            String userId = getCurrentUserId(authentication);
            theaters = theaterService.getTheatersByOwner(userId, pageable);
        } else if (locationId != null) {
            theaters = theaterService.getTheatersByLocation(locationId, pageable);
        } else if (dzongkhag != null) {
            theaters = theaterService.getTheatersByDzongkhag(dzongkhag, pageable);
        } else if (search != null && !search.trim().isEmpty()) {
            theaters = theaterService.searchTheaters(search, pageable);
        } else {
            theaters = theaterService.getAllTheaters(pageable);
        }

        return ResponseEntity.ok(theaters);
    }

    /**
     * Get theaters list (no pagination) - useful for dropdowns
     */
    @GetMapping("/list")
    public ResponseEntity<List<TheaterDTO>> getTheatersList(
            @RequestParam(required = false) String locationId,
            Authentication authentication) {

        List<TheaterDTO> theaters;

        if (hasRole(authentication, "THEATER_OWNER") && !hasRole(authentication, "ADMIN")) {
            String userId = getCurrentUserId(authentication);
            theaters = theaterService.getTheatersByOwnerList(userId);
        } else if (locationId != null) {
            theaters = theaterService.getTheatersByLocationList(locationId);
        } else {
            theaters = theaterService.getAllTheatersList();
        }

        return ResponseEntity.ok(theaters);
    }

    /**
     * Get theater by ID with ownership validation
     */
    @GetMapping("/{id}")
    public ResponseEntity<TheaterDTO> getTheaterById(@PathVariable String id, Authentication authentication) {
        TheaterDTO theater = theaterService.getTheaterById(id);

        // Check if theater owner is accessing their own theater
        if (hasRole(authentication, "THEATER_OWNER") && !hasRole(authentication, "ADMIN")) {
            String userId = getCurrentUserId(authentication);
            if (!theater.getOwnerId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        return ResponseEntity.ok(theater);
    }

    /**
     * Get theater details with halls included
     */
    @GetMapping("/{id}/details")
    public ResponseEntity<TheaterDTO> getTheaterDetails(@PathVariable String id, Authentication authentication) {
        TheaterDTO theater = theaterService.getTheaterWithHalls(id);

        // Check ownership for theater owners
        if (hasRole(authentication, "THEATER_OWNER") && !hasRole(authentication, "ADMIN")) {
            String userId = getCurrentUserId(authentication);
            if (!theater.getOwnerId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        return ResponseEntity.ok(theater);
    }

    /**
     * Get theater statistics (total halls, seats, screenings, etc.)
     */
    @GetMapping("/{id}/stats")
    public ResponseEntity<TheaterStatsDTO> getTheaterStats(@PathVariable String id, Authentication authentication) {
        // Check ownership for theater owners
        if (hasRole(authentication, "THEATER_OWNER") && !hasRole(authentication, "ADMIN")) {
            TheaterDTO theater = theaterService.getTheaterById(id);
            String userId = getCurrentUserId(authentication);
            if (!theater.getOwnerId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        TheaterStatsDTO stats = theaterService.getTheaterStatistics(id);
        return ResponseEntity.ok(stats);
    }

    /**
     * Create new theater
     * Current authenticated user becomes the owner
     */
    @PostMapping
    public ResponseEntity<TheaterDTO> createTheater(@RequestBody @Valid TheaterCreateDTO createDTO,
                                                    Authentication authentication) {
        String ownerId = getCurrentUserId(authentication);
        TheaterDTO createdTheater = theaterService.createTheater(createDTO, ownerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTheater);
    }

    /**
     * Update theater with ownership validation
     */
    @PutMapping("/{id}")
    public ResponseEntity<TheaterDTO> updateTheater(@PathVariable String id,
                                                    @RequestBody @Valid TheaterCreateDTO updateDTO,
                                                    Authentication authentication) {
        // Check ownership for theater owners
        if (hasRole(authentication, "THEATER_OWNER") && !hasRole(authentication, "ADMIN")) {
            TheaterDTO existingTheater = theaterService.getTheaterById(id);
            String userId = getCurrentUserId(authentication);
            if (!existingTheater.getOwnerId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        TheaterDTO updatedTheater = theaterService.updateTheater(id, updateDTO);
        return ResponseEntity.ok(updatedTheater);
    }

    /**
     * Toggle theater active status
     */
    @PatchMapping("/{id}/toggle-active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TheaterDTO> toggleTheaterActive(@PathVariable String id) {
        TheaterDTO updatedTheater = theaterService.toggleTheaterActive(id);
        return ResponseEntity.ok(updatedTheater);
    }

    /**
     * Soft delete theater (Admin only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTheater(@PathVariable String id) {
        theaterService.deleteTheater(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Transfer theater ownership (Admin only)
     */
    @PatchMapping("/{id}/transfer-ownership")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TheaterDTO> transferOwnership(@PathVariable String id,
                                                        @RequestParam String newOwnerId) {
        TheaterDTO updatedTheater = theaterService.transferOwnership(id, newOwnerId);
        return ResponseEntity.ok(updatedTheater);
    }

    /**
     * Get my theaters (for current logged-in theater owner)
     */
    @GetMapping("/my-theaters")
    @PreAuthorize("hasRole('THEATER_OWNER')")
    public ResponseEntity<Page<TheaterDTO>> getMyTheaters(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {

        String userId = getCurrentUserId(authentication);
        Pageable pageable = PageRequest.of(page, size);
        Page<TheaterDTO> theaters = theaterService.getTheatersByOwner(userId, pageable);
        return ResponseEntity.ok(theaters);
    }

    private String getCurrentUserId(Authentication authentication) {
        JwtAuthenticationToken jwtToken = (JwtAuthenticationToken) authentication;
        return jwtToken.getToken().getClaimAsString("sub");
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
    }
}