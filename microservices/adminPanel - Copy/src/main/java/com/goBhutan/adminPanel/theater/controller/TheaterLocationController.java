package com.goBhutan.adminPanel.theater.controller;

import com.goBhutan.adminPanel.common.dto.ApiResponse;
import com.goBhutan.adminPanel.theater.dto.theater.TheaterLocationDTO;
import com.goBhutan.adminPanel.theater.dto.theater.TheaterLocationResponseDTO;
import com.goBhutan.adminPanel.theater.service.TheaterLocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/theater-locations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Theater Location Management", description = "APIs for managing theater locations")
public class TheaterLocationController {

    private final TheaterLocationService locationService;

    @PostMapping
    @Operation(summary = "Create a new theater location")
    public ResponseEntity<ApiResponse<TheaterLocationResponseDTO>> createLocation(
            @Valid @RequestBody TheaterLocationDTO locationDTO
    ) {
        try {
            TheaterLocationResponseDTO createdLocation = locationService.createLocation(locationDTO);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Location created successfully", createdLocation));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));

        } catch (Exception e) {
            log.error("Error creating location: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create location: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a theater location")
    public ResponseEntity<ApiResponse<TheaterLocationResponseDTO>> updateLocation(
            @PathVariable Long id,
            @Valid @RequestBody TheaterLocationDTO locationDTO
    ) {
        try {
            TheaterLocationResponseDTO updatedLocation = locationService.updateLocation(id, locationDTO);
            return ResponseEntity.ok(ApiResponse.success("Location updated successfully", updatedLocation));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));

        } catch (Exception e) {
            log.error("Error updating location: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update location: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a theater location")
    public ResponseEntity<ApiResponse<Void>> deleteLocation(@PathVariable Long id) {
        try {
            locationService.deleteLocation(id);
            return ResponseEntity.ok(ApiResponse.success("Location deleted successfully", null));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));

        } catch (Exception e) {
            log.error("Error deleting location: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to delete location: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get location by ID")
    public ResponseEntity<ApiResponse<TheaterLocationResponseDTO>> getLocationById(@PathVariable Long id) {
        try {
            TheaterLocationResponseDTO location = locationService.getLocationById(id);
            return ResponseEntity.ok(ApiResponse.success(location));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve location: " + e.getMessage()));
        }
    }

    @GetMapping
    @Operation(summary = "Get all locations")
    public ResponseEntity<ApiResponse<List<TheaterLocationResponseDTO>>> getAllLocations() {
        try {
            List<TheaterLocationResponseDTO> locations = locationService.getAllLocations();
            return ResponseEntity.ok(ApiResponse.success(locations));

        } catch (Exception e) {
            log.error("Error retrieving locations: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve locations: " + e.getMessage()));
        }
    }

    @GetMapping("/paginated")
    @Operation(summary = "Get all locations with pagination")
    public ResponseEntity<ApiResponse<Page<TheaterLocationResponseDTO>>> getAllLocationsPaginated(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        try {
            Page<TheaterLocationResponseDTO> locations = locationService.getAllLocationsPaginated(pageable);
            return ResponseEntity.ok(ApiResponse.success(locations));

        } catch (Exception e) {
            log.error("Error retrieving paginated locations: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve locations: " + e.getMessage()));
        }
    }

    @GetMapping("/search")
    @Operation(summary = "Search locations by keyword")
    public ResponseEntity<ApiResponse<List<TheaterLocationResponseDTO>>> searchLocations(
            @RequestParam String keyword
    ) {
        try {
            List<TheaterLocationResponseDTO> locations = locationService.searchLocations(keyword);
            return ResponseEntity.ok(ApiResponse.success(locations));

        } catch (Exception e) {
            log.error("Error searching locations: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to search locations: " + e.getMessage()));
        }
    }

    @GetMapping("/dzongkhag/{dzongkhag}")
    @Operation(summary = "Get locations by dzongkhag")
    public ResponseEntity<ApiResponse<List<TheaterLocationResponseDTO>>> getLocationsByDzongkhag(
            @PathVariable String dzongkhag
    ) {
        try {
            List<TheaterLocationResponseDTO> locations = locationService.getLocationsByDzongkhag(dzongkhag);
            return ResponseEntity.ok(ApiResponse.success(locations));

        } catch (Exception e) {
            log.error("Error retrieving locations by dzongkhag: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve locations: " + e.getMessage()));
        }
    }
}