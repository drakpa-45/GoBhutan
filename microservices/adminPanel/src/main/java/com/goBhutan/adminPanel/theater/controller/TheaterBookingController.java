package com.goBhutan.adminPanel.theater.controller;

import com.goBhutan.adminPanel.theater.dto.booking.BookingRequestDTO;
import com.goBhutan.adminPanel.theater.dto.booking.TicketResponseDTO;
import com.goBhutan.adminPanel.common.dto.ApiResponse;
import com.goBhutan.adminPanel.theater.dto.seat.SeatLockRequestDTO;
import com.goBhutan.adminPanel.theater.dto.seat.SeatLockResponseDTO;
import com.goBhutan.adminPanel.theater.dto.seat.ShowtimeLockedSeatsDTO;
import com.goBhutan.adminPanel.theater.service.SeatLockService;
import com.goBhutan.adminPanel.theater.service.TheaterBookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class TheaterBookingController {

    private final TheaterBookingService bookingService;
    private final SeatLockService seatLockService;

    @PostMapping("/toggle-lock")
    public ResponseEntity<ApiResponse<SeatLockResponseDTO>> toggleLock(
            @Valid @RequestBody SeatLockRequestDTO request) {

        SeatLockResponseDTO response = seatLockService.toggleSeatLock(request);
        String message = "LOCKED".equals(response.getStatus())
                ? "Seat " + response.getSeatIdentifier() +
                " [" + response.getSeatClassName() + "]" +
                " in " + response.getHallName() + " locked for 3 minutes"
                : "Seat " + response.getSeatIdentifier() + " unlocked";

        return ResponseEntity.ok(ApiResponse.success(message, response));
    }

    // Seat map — scoped to hall + showtime, grouped by class
    @GetMapping("/locked/{screenId}/hall/{hallId}")
    public ResponseEntity<ApiResponse<ShowtimeLockedSeatsDTO>> getLockedSeats(
            @PathVariable Long screenId,
            @PathVariable Long hallId) {

        ShowtimeLockedSeatsDTO dto = seatLockService.getLockedSeats(screenId, hallId);
        return ResponseEntity.ok(ApiResponse.success("Locked seats fetched", dto));
    }

    @PostMapping("/book")
    public ResponseEntity<ApiResponse<List<TicketResponseDTO>>> bookTickets(@RequestBody BookingRequestDTO request) {
        List<TicketResponseDTO> tickets = bookingService.bookTickets(request);
        return ResponseEntity.ok(ApiResponse.success("Booking successful", tickets));
    }

    // FETCH BOOKINGS BY THEATER ID
    @GetMapping("/fetchAllbooking/{theaterId}")
    public ResponseEntity<ApiResponse<List<TicketResponseDTO>>> getBookingsByTheater(
            @PathVariable Long theaterId) {
        List<TicketResponseDTO> bookings = bookingService.getBookingsByTheaterId(theaterId);
        return ResponseEntity.ok(ApiResponse.success("Bookings retrieved successfully", bookings));
    }

    // CANCEL SINGLE TICKET
    @PostMapping("/cancel/ticket/{ticketNumber}")
    public ResponseEntity<ApiResponse<Void>> cancelTicket(
            @PathVariable String ticketNumber) {

        bookingService.cancelTicket(ticketNumber);
        return ResponseEntity.ok(ApiResponse.success("Ticket cancelled successfully", null));
    }

    // CANCEL ENTIRE BOOKING
    @PostMapping("/cancel/booking/{bookingRef}")
    public ResponseEntity<ApiResponse<Void>> cancelBooking(
            @PathVariable String bookingRef) {

        bookingService.cancelBooking(bookingRef);
        return ResponseEntity.ok(ApiResponse.success("Booking cancelled successfully", null));
    }
}
