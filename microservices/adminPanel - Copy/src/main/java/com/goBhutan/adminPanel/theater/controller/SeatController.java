package com.goBhutan.adminPanel.theater.controller;

import com.goBhutan.adminPanel.common.dto.ApiResponse;
import com.goBhutan.adminPanel.theater.dto.seat.SeatBlockRequestDTO;
import com.goBhutan.adminPanel.theater.dto.seat.SeatDTO;
import com.goBhutan.adminPanel.theater.dto.seat.SeatLayoutResponseDTO;
import com.goBhutan.adminPanel.theater.layout.SeatLayoutRequest;
import com.goBhutan.adminPanel.theater.service.SeatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seats")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Seat Management", description = "APIs for managing theater seats")
public class SeatController {

    private final SeatService seatService;

    @PostMapping("/configure")
    @Operation(summary = "Configure/regenerate seat layout for a hall")
    public ResponseEntity<ApiResponse<SeatLayoutResponseDTO>> configureSeats(
            @Valid @RequestBody SeatLayoutRequest request
    ) {
        try {
            log.info("Configuring seats for hall ID: {}", request.getHallId());

            SeatLayoutResponseDTO response = seatService.configureSeats(request);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Seat layout configured successfully", response));

        } catch (IllegalArgumentException e) {
            log.error("Validation error configuring seats: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));

        } catch (Exception e) {
            log.error("Error configuring seats: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to configure seats: " + e.getMessage()));
        }
    }

    @GetMapping("/hall/{hallId}/layout")
    @Operation(summary = "Get complete seat layout for a hall")
    public ResponseEntity<ApiResponse<SeatLayoutResponseDTO>> getSeatLayout(
            @PathVariable Long hallId
    ) {
        try {
            SeatLayoutResponseDTO layout = seatService.getSeatLayoutByHall(hallId);
            return ResponseEntity.ok(ApiResponse.success(layout));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));

        } catch (Exception e) {
            log.error("Error retrieving seat layout: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve seat layout: " + e.getMessage()));
        }
    }

    @GetMapping("/hall/{hallId}")
    @Operation(summary = "Get all seats of a hall")
    public ResponseEntity<ApiResponse<List<SeatDTO>>> getSeatsByHall(
            @PathVariable Long hallId
    ) {
        try {
            List<SeatDTO> seats = seatService.getSeatsByHall(hallId);
            return ResponseEntity.ok(ApiResponse.success(seats));

        } catch (Exception e) {
            log.error("Error retrieving seats: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve seats: " + e.getMessage()));
        }
    }

    @GetMapping("/hall/{hallId}/row/{rowName}")
    @Operation(summary = "Get seats by hall and row")
    public ResponseEntity<ApiResponse<List<SeatDTO>>> getSeatsByHallAndRow(
            @PathVariable Long hallId,
            @PathVariable String rowName
    ) {
        try {
            List<SeatDTO> seats = seatService.getSeatsByHallAndRow(hallId, rowName);
            return ResponseEntity.ok(ApiResponse.success(seats));

        } catch (Exception e) {
            log.error("Error retrieving seats by row: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve seats: " + e.getMessage()));
        }
    }

    @PutMapping("/{seatId}/block")
    @Operation(summary = "Block or unblock a seat")
    public ResponseEntity<ApiResponse<SeatDTO>> blockSeat(
            @PathVariable Long seatId,
            @Valid @RequestBody SeatBlockRequestDTO request
    ) {
        try {
            log.info("Updating block status for seat ID: {}", seatId);

            SeatDTO updatedSeat = seatService.blockSeat(seatId, request);

            return ResponseEntity.ok(ApiResponse.success(
                    "Seat status updated successfully",
                    updatedSeat
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));

        } catch (Exception e) {
            log.error("Error updating seat status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update seat status: " + e.getMessage()));
        }
    }

    @PutMapping("/block-multiple")
    @Operation(summary = "Block or unblock multiple seats")
    public ResponseEntity<ApiResponse<List<SeatDTO>>> blockMultipleSeats(
            @RequestParam List<Long> seatIds,
            @Valid @RequestBody SeatBlockRequestDTO request
    ) {
        try {
            log.info("Updating block status for {} seats", seatIds.size());

            List<SeatDTO> updatedSeats = seatService.blockMultipleSeats(seatIds, request);

            return ResponseEntity.ok(ApiResponse.success(
                    "Seats updated successfully",
                    updatedSeats
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));

        } catch (Exception e) {
            log.error("Error updating seats: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update seats: " + e.getMessage()));
        }
    }

    @GetMapping("/hall/{hallId}/blocked")
    @Operation(summary = "Get blocked seats by hall")
    public ResponseEntity<ApiResponse<List<SeatDTO>>> getBlockedSeats(
            @PathVariable Long hallId
    ) {
        try {
            List<SeatDTO> seats = seatService.getBlockedSeatsByHall(hallId);
            return ResponseEntity.ok(ApiResponse.success(seats));

        } catch (Exception e) {
            log.error("Error retrieving blocked seats: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve blocked seats: " + e.getMessage()));
        }
    }

    @GetMapping("/hall/{hallId}/available")
    @Operation(summary = "Get available seats by hall")
    public ResponseEntity<ApiResponse<List<SeatDTO>>> getAvailableSeats(
            @PathVariable Long hallId
    ) {
        try {
            List<SeatDTO> seats = seatService.getAvailableSeatsByHall(hallId);
            return ResponseEntity.ok(ApiResponse.success(seats));

        } catch (Exception e) {
            log.error("Error retrieving available seats: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve available seats: " + e.getMessage()));
        }
    }

    @GetMapping("/hall/{hallId}/seat/{identifier}")
    @Operation(summary = "Get seat by identifier (e.g., A5)")
    public ResponseEntity<ApiResponse<SeatDTO>> getSeatByIdentifier(
            @PathVariable Long hallId,
            @PathVariable String identifier
    ) {
        try {
            SeatDTO seat = seatService.getSeatByIdentifier(hallId, identifier);
            return ResponseEntity.ok(ApiResponse.success(seat));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));

        } catch (Exception e) {
            log.error("Error retrieving seat: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve seat: " + e.getMessage()));
        }
    }

    @DeleteMapping("/hall/{hallId}")
    @Operation(summary = "Delete all seats from a hall")
    public ResponseEntity<ApiResponse<Void>> deleteAllSeats(
            @PathVariable Long hallId
    ) {
        try {
            log.info("Deleting all seats from hall ID: {}", hallId);

            seatService.deleteAllSeatsFromHall(hallId);

            return ResponseEntity.ok(ApiResponse.success(
                    "All seats deleted successfully",
                    null
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));

        } catch (Exception e) {
            log.error("Error deleting seats: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to delete seats: " + e.getMessage()));
        }
    }
}