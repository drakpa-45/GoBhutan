package com.goBhutan.adminPanel.theater.controller;

import com.goBhutan.adminPanel.theater.dto.booking.BookingRequestDTO;
import com.goBhutan.adminPanel.theater.dto.booking.TicketResponseDTO;
import com.goBhutan.adminPanel.common.dto.ApiResponse;
import com.goBhutan.adminPanel.theater.service.TheaterBookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class TheaterBookingController {

    private final TheaterBookingService bookingService;

    @PostMapping("/book")
    public ResponseEntity<ApiResponse<List<TicketResponseDTO>>> bookTickets(@RequestBody BookingRequestDTO request) {
        List<TicketResponseDTO> tickets = bookingService.bookTickets(request);
        return ResponseEntity.ok(ApiResponse.success("Booking successful", tickets));
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
