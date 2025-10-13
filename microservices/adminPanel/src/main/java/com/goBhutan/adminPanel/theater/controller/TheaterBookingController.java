package com.goBhutan.adminPanel.theater.controller;
import com.goBhutan.adminPanel.theater.dto.*;
import com.goBhutan.adminPanel.theater.entity.TheaterBooking;
import com.goBhutan.adminPanel.theater.service.TheaterBookingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/admin/bookings")
@PreAuthorize("hasRole('ADMIN') or hasRole('THEATER_OWNER') or hasRole('USER')")
public class TheaterBookingController {

    private final TheaterBookingService bookingService;

    public TheaterBookingController(TheaterBookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('THEATER_OWNER')")
    public ResponseEntity<Page<BookingDTO>> getAllBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String screeningId,
            @RequestParam(required = false) String status) {

        Pageable pageable = PageRequest.of(page, size);
        Page<BookingDTO> bookings;

        if (userId != null) {
            bookings = bookingService.getBookingsByUser(userId, pageable);
        } else if (screeningId != null) {
            bookings = bookingService.getBookingsByScreening(screeningId, pageable);
        } else if (status != null) {
            TheaterBooking.BookingStatus bookingStatus = TheaterBooking.BookingStatus.valueOf(status.toUpperCase());
            bookings = bookingService.getBookingsByStatus(bookingStatus, pageable);
        } else {
            bookings = bookingService.getAllBookings(pageable);
        }

        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/my-bookings")
    public ResponseEntity<Page<BookingDTO>> getMyBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {

        // 🔹 Extract logged-in user ID from Keycloak token
        Jwt principal = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = principal.getSubject(); // Keycloak "sub" claim

        Pageable pageable = PageRequest.of(page, size);
        Page<BookingDTO> bookings = bookingService.getBookingsByUser(userId, pageable);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingDTO> getBookingById(@PathVariable String id,
                                                     Authentication authentication) {
        BookingDTO booking = bookingService.getBookingById(id);

        // 🔹 Extract logged-in user ID from Keycloak token
        Jwt principal = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = principal.getSubject(); // Keycloak "sub" claim
            if (!booking.getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

        return ResponseEntity.ok(booking);
    }

    @GetMapping("/reference/{bookingReference}")
    public ResponseEntity<BookingDTO> getBookingByReference(@PathVariable String bookingReference,
                                                            Authentication authentication) {
        BookingDTO booking = bookingService.getBookingByReference(bookingReference);

        // 🔹 Extract logged-in user ID from Keycloak token
        Jwt principal = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = principal.getSubject(); // Keycloak "sub" claim
            if (!booking.getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

        return ResponseEntity.ok(booking);
    }

    @GetMapping("/screening/{screeningId}/confirmed")
    @PreAuthorize("hasRole('ADMIN') or hasRole('THEATER_OWNER')")
    public ResponseEntity<List<BookingDTO>> getConfirmedBookingsForScreening(@PathVariable String screeningId) {
        List<BookingDTO> bookings = bookingService.getConfirmedBookingsForScreening(screeningId);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/screening/{screeningId}/available-seats")
    public ResponseEntity<List<SeatAvailabilityDTO>> getAvailableSeats(@PathVariable String screeningId) {
        List<SeatAvailabilityDTO> availableSeats = bookingService.getAvailableSeatsForScreening(screeningId);
        return ResponseEntity.ok(availableSeats);
    }

    @GetMapping("/seat-availability")
    public ResponseEntity<Boolean> checkSeatAvailability(@RequestParam String screeningId,
                                                         @RequestParam String seatId) {
        boolean isAvailable = bookingService.isSeatAvailable(screeningId, seatId);
        return ResponseEntity.ok(isAvailable);
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN') or hasRole('THEATER_OWNER')")
    public ResponseEntity<BookingStatsDTO> getBookingStats() {
        BookingStatsDTO stats = bookingService.getBookingStats();
        return ResponseEntity.ok(stats);
    }

    @PostMapping
    public ResponseEntity<BookingResponseDTO> createBooking(@RequestBody @Valid BookingCreateDTO createDTO,
                                                            Authentication authentication) {
        // 🔹 Extract logged-in user ID from Keycloak token
        Jwt principal = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = principal.getSubject(); // Keycloak "sub" claim
        BookingResponseDTO response = bookingService.createBooking(createDTO, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<BookingDTO> cancelBooking(@PathVariable String id,
                                                    @RequestBody(required = false) CancelBookingDTO cancelDTO,
                                                    Authentication authentication) {
        // 🔹 Extract logged-in user ID from Keycloak token
        Jwt principal = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = principal.getSubject(); // Keycloak "sub" claim
            BookingDTO existingBooking = bookingService.getBookingById(id);
            if (!existingBooking.getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

        String reason = cancelDTO != null ? cancelDTO.getReason() : "User cancellation";
        BookingDTO cancelledBooking = bookingService.cancelBooking(id, reason);
        return ResponseEntity.ok(cancelledBooking);
    }
}