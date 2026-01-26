package com.goBhutan.adminPanel.theater.controller;

import com.goBhutan.adminPanel.common.dto.ApiResponse;
import com.goBhutan.adminPanel.theater.dto.theater.TheaterDTO;
import com.goBhutan.adminPanel.theater.dto.theater.TheaterResponseDTO;
import com.goBhutan.adminPanel.theater.dto.theater.TheaterSummaryDTO;
import com.goBhutan.adminPanel.theater.dto.theater.TheaterUpdateDTO;
import com.goBhutan.adminPanel.theater.service.TheaterService;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/theaters")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Theater Management", description = "APIs for managing theaters")
public class TheaterController {

    private final TheaterService theaterService;

    @PostMapping
    @Operation(summary = "Create a new theater")
    public ResponseEntity<ApiResponse<TheaterResponseDTO>> createTheater(
            @Valid @RequestBody TheaterDTO theaterDTO
    ) {
        try {
            log.info("Creating theater: {}", theaterDTO.getName());

            Jwt principal = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String adminUserId = principal.getSubject();
            theaterDTO.setAdminUserId(adminUserId);

            TheaterResponseDTO createdTheater = theaterService.createTheater(theaterDTO);

            ApiResponse<TheaterResponseDTO> response = ApiResponse.success(
                    "Theater created successfully",
                    createdTheater
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            log.error("Validation error creating theater: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Validation error: " + e.getMessage()));

        } catch (Exception e) {
            log.error("Error creating theater: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create theater: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing theater")
    public ResponseEntity<ApiResponse<TheaterResponseDTO>> updateTheater(
            @PathVariable Long id,
            @Valid @RequestBody TheaterUpdateDTO updateDTO
    ) {
        try {
            log.info("Updating theater with ID: {}", id);
            Jwt principal = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String adminUserId = principal.getSubject();
            updateDTO.setAdminUserId(adminUserId);

            TheaterResponseDTO updatedTheater = theaterService.updateTheater(id, updateDTO);

            ApiResponse<TheaterResponseDTO> response = ApiResponse.success(
                    "Theater updated successfully",
                    updatedTheater
            );

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Theater not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));

        } catch (Exception e) {
            log.error("Error updating theater: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update theater: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete a theater")
    public ResponseEntity<ApiResponse<Void>> deleteTheater(@PathVariable Long id) {
        try {
            log.info("Soft deleting theater with ID: {}", id);

            theaterService.softDeleteTheater(id);

            ApiResponse<Void> response = ApiResponse.success(
                    "Theater deleted successfully",
                    null
            );

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Theater not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));

        } catch (Exception e) {
            log.error("Error deleting theater: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to delete theater: " + e.getMessage()));
        }
    }

    @PatchMapping("/{id}/restore")
    @Operation(summary = "Restore a soft deleted theater")
    public ResponseEntity<ApiResponse<Void>> restoreTheater(@PathVariable Long id) {
        try {
            log.info("Restoring theater with ID: {}", id);

            theaterService.restoreTheater(id);

            ApiResponse<Void> response = ApiResponse.success(
                    "Theater restored successfully",
                    null
            );

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to restore theater: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get theater by ID")
    public ResponseEntity<ApiResponse<TheaterResponseDTO>> getTheaterById(@PathVariable Long id) {
        try {
            TheaterResponseDTO theater = theaterService.getTheaterById(id);
            return ResponseEntity.ok(ApiResponse.success(theater));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve theater: " + e.getMessage()));
        }
    }

    @GetMapping
    @Operation(summary = "Get all active theaters")
    public ResponseEntity<ApiResponse<List<TheaterSummaryDTO>>> getAllTheaters() {
        try {
            List<TheaterSummaryDTO> theaters = theaterService.getAllActiveTheaters();
            return ResponseEntity.ok(ApiResponse.success(theaters));

        } catch (Exception e) {
            log.error("Error retrieving theaters: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve theaters: " + e.getMessage()));
        }
    }

    @GetMapping("/paginated")
    @Operation(summary = "Get all theaters with pagination")
    public ResponseEntity<ApiResponse<Page<TheaterSummaryDTO>>> getAllTheatersPaginated(
            @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        try {
            Page<TheaterSummaryDTO> theaters = theaterService.getAllTheatersPaginated(pageable);
            return ResponseEntity.ok(ApiResponse.success(theaters));

        } catch (Exception e) {
            log.error("Error retrieving paginated theaters: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve theaters: " + e.getMessage()));
        }
    }

    @GetMapping("/location/{locationId}")
    @Operation(summary = "Get theaters by location")
    public ResponseEntity<ApiResponse<List<TheaterSummaryDTO>>> getTheatersByLocation(
            @PathVariable Long locationId
    ) {
        try {
            List<TheaterSummaryDTO> theaters = theaterService.getTheatersByLocation(locationId);
            return ResponseEntity.ok(ApiResponse.success(theaters));

        } catch (Exception e) {
            log.error("Error retrieving theaters by location: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve theaters: " + e.getMessage()));
        }
    }

    @GetMapping("/search")
    @Operation(summary = "Search theaters by name")
    public ResponseEntity<ApiResponse<List<TheaterSummaryDTO>>> searchTheaters(
            @RequestParam String name
    ) {
        try {
            List<TheaterSummaryDTO> theaters = theaterService.searchTheaters(name);
            return ResponseEntity.ok(ApiResponse.success(theaters));

        } catch (Exception e) {
            log.error("Error searching theaters: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to search theaters: " + e.getMessage()));
        }
    }
}