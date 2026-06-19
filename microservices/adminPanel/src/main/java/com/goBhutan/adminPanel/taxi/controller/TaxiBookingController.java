package com.goBhutan.adminPanel.taxi.controller;

import com.goBhutan.adminPanel.taxi.dto.request.BookingRequest;
import com.goBhutan.adminPanel.taxi.dto.response.BookingResponse;
import com.goBhutan.adminPanel.taxi.service.TaxiBookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/taxi/bookings")
@RequiredArgsConstructor
public class TaxiBookingController {

    private final TaxiBookingService bookingService;

    /**
     * POST /api/yaya/bookings
     * Handles all 4 modes: INTRA+PULL, INTRA+RESERVED, INTER+PULL, INTER+RESERVED
     *
     * Body varies by mode:
     *  - Intra Pull:      tripCategory=INTRA, tripMode=PULL, distanceKm, paymentMethod
     *  - Intra Reserved:  + scheduledPickupTime
     *  - Inter Pull:      tripCategory=INTER, tripMode=PULL, interRouteId, seatsBooked
     *  - Inter Reserved:  tripCategory=INTER, tripMode=RESERVED, interRouteId
     */
    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody BookingRequest request) {
        return ResponseEntity.ok(bookingService.createBooking(request));
    }

    /** Driver accepts the trip (Pull mode) */
    @PatchMapping("/{id}/accept")
    public ResponseEntity<BookingResponse> acceptBooking(
            @PathVariable Long id,
            @RequestParam Long driverId) {
        return ResponseEntity.ok(bookingService.startTrip(id, driverId));
    }

    /** Driver starts the trip */
    @PatchMapping("/{id}/start")
    public ResponseEntity<BookingResponse> startTrip(
            @PathVariable Long id,
            @RequestParam Long driverId) {
        return ResponseEntity.ok(bookingService.startTrip(id, driverId));
    }

    /** Driver or system completes the trip — collects balance for Reserved */
    @PatchMapping("/{id}/complete")
    public ResponseEntity<BookingResponse> completeTrip(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.completeTrip(id));
    }

    /** Cancel a booking (passenger or driver) */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(
            @PathVariable Long id,
            @RequestParam(required = false) String reason,
            @RequestParam(defaultValue = "false") boolean byDriver) {
        return ResponseEntity.ok(bookingService.cancelBooking(id, reason, byDriver));
    }

    /** Driver confirms cash received */
    @PatchMapping("/{id}/confirm-cash")
    public ResponseEntity<Void> confirmCash(@PathVariable Long id) {
        bookingService.confirmCashReceived(id);
        return ResponseEntity.ok().build();
    }
}
