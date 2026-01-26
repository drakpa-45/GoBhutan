package com.goBhutan.adminPanel.theater.controller;

import com.goBhutan.adminPanel.common.dto.ApiResponse;
import com.goBhutan.adminPanel.theater.dto.hall.HallDTO;
import com.goBhutan.adminPanel.theater.dto.hall.HallResponseDTO;
import com.goBhutan.adminPanel.theater.dto.hall.HallSummaryDTO;
import com.goBhutan.adminPanel.theater.dto.hall.HallUpdateDTO;
import com.goBhutan.adminPanel.theater.service.HallService;
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
@RequestMapping("/api/halls")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Hall Management", description = "APIs for managing theater halls")
public class HallController {

    private final HallService hallService;

    @PostMapping
    @Operation(summary = "Create a new hall")
    public ResponseEntity<ApiResponse<HallResponseDTO>> createHall(
            @Valid @RequestBody HallDTO hallDTO
    ) {
        try {
            log.info("Creating hall: {}", hallDTO.getName());

            HallResponseDTO createdHall = hallService.createHall(hallDTO);

            ApiResponse<HallResponseDTO> response = ApiResponse.success(
                    "Hall created successfully",
                    createdHall
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            log.error("Validation error creating hall: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));

        } catch (Exception e) {
            log.error("Error creating hall: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create hall: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing hall")
    public ResponseEntity<ApiResponse<HallResponseDTO>> updateHall(
            @PathVariable Long id,
            @Valid @RequestBody HallUpdateDTO updateDTO
    ) {
        try {
            log.info("Updating hall with ID: {}", id);

            HallResponseDTO updatedHall = hallService.updateHall(id, updateDTO);

            ApiResponse<HallResponseDTO> response = ApiResponse.success(
                    "Hall updated successfully",
                    updatedHall
            );

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Error updating hall: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));

        } catch (Exception e) {
            log.error("Error updating hall: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update hall: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}/soft")
    @Operation(summary = "Soft delete a hall")
    public ResponseEntity<ApiResponse<Void>> softDeleteHall(@PathVariable Long id) {
        try {
            log.info("Soft deleting hall with ID: {}", id);

            hallService.softDeleteHall(id);

            ApiResponse<Void> response = ApiResponse.success(
                    "Hall soft deleted successfully",
                    null
            );

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));

        } catch (Exception e) {
            log.error("Error soft deleting hall: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to soft delete hall: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Permanently delete a hall")
    public ResponseEntity<ApiResponse<Void>> deleteHall(@PathVariable Long id) {
        try {
            log.info("Deleting hall with ID: {}", id);

            hallService.deleteHall(id);

            ApiResponse<Void> response = ApiResponse.success(
                    "Hall deleted successfully",
                    null
            );

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));

        } catch (Exception e) {
            log.error("Error deleting hall: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to delete hall: " + e.getMessage()));
        }
    }

    @PatchMapping("/{id}/restore")
    @Operation(summary = "Restore a soft deleted hall")
    public ResponseEntity<ApiResponse<Void>> restoreHall(@PathVariable Long id) {
        try {
            log.info("Restoring hall with ID: {}", id);

            hallService.restoreHall(id);

            ApiResponse<Void> response = ApiResponse.success(
                    "Hall restored successfully",
                    null
            );

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to restore hall: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get hall by ID")
    public ResponseEntity<ApiResponse<HallResponseDTO>> getHallById(@PathVariable Long id) {
        try {
            HallResponseDTO hall = hallService.getHallById(id);
            return ResponseEntity.ok(ApiResponse.success(hall));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));

        } catch (Exception e) {
            log.error("Error retrieving hall: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve hall: " + e.getMessage()));
        }
    }

    @GetMapping
    @Operation(summary = "Get all active halls")
    public ResponseEntity<ApiResponse<List<HallSummaryDTO>>> getAllActiveHalls() {
        try {
            List<HallSummaryDTO> halls = hallService.getAllActiveHalls();
            return ResponseEntity.ok(ApiResponse.success(halls));

        } catch (Exception e) {
            log.error("Error retrieving halls: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve halls: " + e.getMessage()));
        }
    }

    @GetMapping("/paginated")
    @Operation(summary = "Get all halls with pagination")
    public ResponseEntity<ApiResponse<Page<HallSummaryDTO>>> getAllHallsPaginated(
            @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        try {
            Page<HallSummaryDTO> halls = hallService.getAllHallsPaginated(pageable);
            return ResponseEntity.ok(ApiResponse.success(halls));

        } catch (Exception e) {
            log.error("Error retrieving paginated halls: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve halls: " + e.getMessage()));
        }
    }

    @GetMapping("/theater/{theaterId}")
    @Operation(summary = "Get active halls by theater")
    public ResponseEntity<ApiResponse<List<HallSummaryDTO>>> getHallsByTheater(
            @PathVariable Long theaterId
    ) {
        try {
            List<HallSummaryDTO> halls = hallService.getHallsByTheater(theaterId);
            return ResponseEntity.ok(ApiResponse.success(halls));

        } catch (Exception e) {
            log.error("Error retrieving halls by theater: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve halls: " + e.getMessage()));
        }
    }

    @GetMapping("/theater/{theaterId}/all")
    @Operation(summary = "Get all halls by theater (including inactive)")
    public ResponseEntity<ApiResponse<List<HallResponseDTO>>> getAllHallsByTheater(
            @PathVariable Long theaterId
    ) {
        try {
            List<HallResponseDTO> halls = hallService.getAllHallsByTheater(theaterId);
            return ResponseEntity.ok(ApiResponse.success(halls));

        } catch (Exception e) {
            log.error("Error retrieving all halls by theater: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve halls: " + e.getMessage()));
        }
    }

    @GetMapping("/theater/{theaterId}/paginated")
    @Operation(summary = "Get halls by theater with pagination")
    public ResponseEntity<ApiResponse<Page<HallResponseDTO>>> getHallsByTheaterPaginated(
            @PathVariable Long theaterId,
            @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        try {
            Page<HallResponseDTO> halls = hallService.getHallsByTheaterPaginated(theaterId, pageable);
            return ResponseEntity.ok(ApiResponse.success(halls));

        } catch (Exception e) {
            log.error("Error retrieving paginated halls by theater: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve halls: " + e.getMessage()));
        }
    }

    @GetMapping("/search")
    @Operation(summary = "Search halls by name")
    public ResponseEntity<ApiResponse<List<HallSummaryDTO>>> searchHalls(
            @RequestParam String name
    ) {
        try {
            List<HallSummaryDTO> halls = hallService.searchHalls(name);
            return ResponseEntity.ok(ApiResponse.success(halls));

        } catch (Exception e) {
            log.error("Error searching halls: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to search halls: " + e.getMessage()));
        }
    }

    @GetMapping("/theater/{theaterId}/count")
    @Operation(summary = "Get hall count by theater")
    public ResponseEntity<ApiResponse<Long>> getHallCountByTheater(
            @PathVariable Long theaterId
    ) {
        try {
            long count = hallService.getHallCountByTheater(theaterId);
            return ResponseEntity.ok(ApiResponse.success(count));

        } catch (Exception e) {
            log.error("Error getting hall count: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to get hall count: " + e.getMessage()));
        }
    }
}