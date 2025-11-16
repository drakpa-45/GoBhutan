package com.goBhutan.adminPanel.busAdmin.controller;

import com.goBhutan.adminPanel.busAdmin.dto.*;
import com.goBhutan.adminPanel.busAdmin.service.BookingsService;
import com.goBhutan.adminPanel.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bus/bookings")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BookingController_new {
    private final BookingsService bookingService;
    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponseDTO>> createBooking(
            @Valid @RequestBody BookingRequestDTO request) {
        try {

            BookingResponseDTO booking = bookingService.createBooking(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "Booking created successfully", booking));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @GetMapping("/schedule/{scheduleId}/available-seats")
    public ResponseEntity<ApiResponse<AvailableSeatsResponseDTO>> getAvailableSeats(
            @PathVariable Long scheduleId) {
        try {
            AvailableSeatsResponseDTO availableSeats = bookingService.getAvailableSeats(scheduleId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Available seats retrieved", availableSeats));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @GetMapping("/reference/{bookingReference}")
    public ResponseEntity<ApiResponse<BookingResponseDTO>> getBookingByReference(
            @PathVariable String bookingReference) {
        try {
            BookingResponseDTO booking = bookingService.getBookingByReference(bookingReference);
            return ResponseEntity.ok(new ApiResponse<>(true, "Booking retrieved", booking));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<ApiResponse<BookingResponseDTO>> getBookingById(
            @PathVariable Long bookingId) {
        try {
            BookingResponseDTO booking = bookingService.getBookingById(bookingId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Booking retrieved", booking));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<BookingResponseDTO>>> getUserBookings(
            @PathVariable String userId) {
        try {
            List<BookingResponseDTO> bookings = bookingService.getUserBookings(userId);
            return ResponseEntity.ok(new ApiResponse<>(true, "User bookings retrieved", bookings));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<List<BookingResponseDTO>>> getBookingsByEmail(
            @PathVariable String email) {
        try {
            List<BookingResponseDTO> bookings = bookingService.getBookingsByEmail(email);
            return ResponseEntity.ok(new ApiResponse<>(true, "Bookings retrieved by email", bookings));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BookingResponseDTO>>> getAllBookings() {
        try {
            List<BookingResponseDTO> bookings = bookingService.getAllBookings();
            return ResponseEntity.ok(new ApiResponse<>(true, "All bookings retrieved", bookings));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @PutMapping("/{bookingId}/cancel")
    public ResponseEntity<ApiResponse<BookingResponseDTO>> cancelBooking(
            @PathVariable Long bookingId,
            @RequestBody(required = false) CancellationRequestDTO request) {
        try {
            String reason = request != null ? request.getCancellationReason() : null;
            BookingResponseDTO booking = bookingService.cancelBooking(bookingId, reason);
            return ResponseEntity.ok(new ApiResponse<>(true, "Booking cancelled successfully", booking));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @PutMapping("/{bookingId}/change-seat")
    public ResponseEntity<ApiResponse<BookingResponseDTO>> changeSeat(
            @PathVariable Long bookingId,
            @Valid @RequestBody SeatChangeRequestDTO request) {
        try {
            BookingResponseDTO booking = bookingService.changeSeat(
                    bookingId,
                    request.getOldSeatNumber(),
                    request.getNewSeatNumber()
            );
            return ResponseEntity.ok(new ApiResponse<>(true, "Seat changed successfully", booking));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }
}
