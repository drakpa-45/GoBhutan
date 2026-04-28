package com.goBhutan.adminPanel.busAdmin.controller;

import com.goBhutan.adminPanel.busAdmin.dto.BusTicketResponse;
import com.goBhutan.adminPanel.busAdmin.dto.ConfirmBookingRequest;
import com.goBhutan.adminPanel.busAdmin.dto.LockSeatRequest;
import com.goBhutan.adminPanel.busAdmin.entity.SeatBooking;
import com.goBhutan.adminPanel.busAdmin.service.BusBookingService;
import com.goBhutan.adminPanel.busAdmin.service.TicketService;
import com.goBhutan.adminPanel.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BusBookingController {

    private final BusBookingService bookingService;
    private final TicketService ticketService;

    @PostMapping("/lock")
    public ResponseEntity<?> lock(@RequestBody LockSeatRequest req) {
        String userId = currentUserId();

        List<SeatBooking> bookings = bookingService.lockSeats(
                req.getScheduleId(),
                req.getSeatNumbers(),
                req.getSeatLabels(),
                userId,
                req.getApplicantCid(),
                req.getApplicantMobile(),
                req.getApplicantEmail());

        return ResponseEntity.ok(Map.of(
                "bookingRef", bookings.get(0).getBookingRef(),
                "seatLabel", bookings.get(0).getSeatLabel(),
                "expiresAt", bookings.get(0).getLockExpiry(),
                "totalAmount", bookings.stream()
                        .map(SeatBooking::getFinalFareAtBooking)
                        .reduce(BigDecimal.ZERO, BigDecimal::add),
                "seats", bookings));
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> confirm(@RequestBody ConfirmBookingRequest req) {
        String userId = currentUserId();

        List<SeatBooking> bookings = bookingService.confirmBooking(req.getBookingRef(), userId);

        return ResponseEntity.ok(Map.of(
                "bookingRef", req.getBookingRef(),
                "paymentRef", bookings.get(0).getWalletPaymentRef(),
                "scheduleId", bookings.get(0).getSchedule().getId(),
                "totalSeats", bookings.size(),
                "seats", bookings.stream().map(b -> Map.of(
                        "bookingId", b.getId(),
                        "seatNumber", b.getSeatNumber(),
                        "seatLabel", b.getSeatLabel(),
                        "status", b.getStatus())).toList()));
    }

    @PostMapping("/cancel/{bookingId}")
    public ResponseEntity<?> cancel(@PathVariable Long bookingId) {
        String userId = currentUserId();

        SeatBooking booking = bookingService.cancel(bookingId, userId);

        return ResponseEntity.ok(Map.of(
                "bookingId", booking.getId(),
                "status", "CANCELLED"));
    }

    @GetMapping("/schedule/{scheduleId}/seats")
    public ResponseEntity<?> getSeatStatus(@PathVariable Long scheduleId) {
        return ResponseEntity.ok(bookingService.getSeatStatus(scheduleId));
    }

    @GetMapping("/ticket/{bookingId}")
    public ResponseEntity<ApiResponse<BusTicketResponse>> getTicket(@PathVariable Long bookingId) {
        return ResponseEntity.ok(ApiResponse.success(ticketService.getTicketDetails(bookingId, currentUserId())));
    }

    @GetMapping("/admin/schedule/{id}/manifest")
    public ResponseEntity<?> getManifest(@PathVariable Long id) {

        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String adminUserId = jwt.getSubject();

        return ResponseEntity.ok(bookingService.getManifestForSchedule(id, adminUserId));
    }

    private String currentUserId() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return jwt.getSubject();
    }
}
