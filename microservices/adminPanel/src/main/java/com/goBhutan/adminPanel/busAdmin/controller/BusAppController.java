package com.goBhutan.adminPanel.busAdmin.controller;

import com.goBhutan.adminPanel.busAdmin.dto.AppScheduleResponse;
import com.goBhutan.adminPanel.busAdmin.dto.BusRouteResponse;
import com.goBhutan.adminPanel.busAdmin.dto.BusTicketResponse;
import com.goBhutan.adminPanel.busAdmin.dto.ConfirmBookingRequest;
import com.goBhutan.adminPanel.busAdmin.dto.LockSeatRequest;
import com.goBhutan.adminPanel.busAdmin.entity.SeatBooking;
import com.goBhutan.adminPanel.busAdmin.service.BusBookingService;
import com.goBhutan.adminPanel.busAdmin.service.BusRouteServiceNew;
import com.goBhutan.adminPanel.busAdmin.service.BusScheduleService;
import com.goBhutan.adminPanel.busAdmin.service.TicketService;
import com.goBhutan.adminPanel.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/api/bus-app")
public class BusAppController {

    private final BusRouteServiceNew busRouteService;
    private final BusScheduleService scheduleService;
    private final BusBookingService bookingService;
    private final TicketService ticketService;

    @GetMapping("/routes/search")
    public ResponseEntity<ApiResponse<List<BusRouteResponse>>> searchActiveRoutes(
            @RequestParam String source,
            @RequestParam String destination,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            List<BusRouteResponse> routes = busRouteService
                    .getBookableSchedulesBySourceDestinationAndDate(source, destination, date);
            return ResponseEntity.ok(ApiResponse.success(routes));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/routes/{routeId}/schedules")
    public ResponseEntity<ApiResponse<List<AppScheduleResponse>>> getAvailableSchedules(
            @PathVariable Long routeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.success(
                scheduleService.getAvailableSchedulesForApp(routeId, date)));
    }

    @GetMapping("/schedules/{scheduleId}/seats")
    public ResponseEntity<?> getSeatStatus(@PathVariable Long scheduleId) {
        return ResponseEntity.ok(bookingService.getSeatStatus(scheduleId));
    }

    @PostMapping("/bookings/lock")
    public ResponseEntity<?> lockSeats(@RequestBody LockSeatRequest req) {
        String userId = currentUserId();

        List<SeatBooking> bookings = bookingService.lockSeats(  
                req.getScheduleId(),
                req.getSeatNumbers(),
                userId,
                req.getApplicantCids(),
                req.getApplicantNames(),
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

    @PostMapping("/bookings/confirm")
    public ResponseEntity<?> confirmBooking(@RequestBody ConfirmBookingRequest req) {
        return confirmWithPaymentMethod(req);
    }

    @PostMapping("/bookings/pay")
    public ResponseEntity<?> payBooking(@RequestBody ConfirmBookingRequest req) {
        return confirmWithPaymentMethod(req);
    }

    @PostMapping("/bookings/{bookingId}/cancel")
    public ResponseEntity<?> cancelBooking(@PathVariable Long bookingId) {
        String userId = currentUserId();

        SeatBooking booking = bookingService.cancel(bookingId, userId);

        return ResponseEntity.ok(Map.of(
                "bookingId", booking.getId(),
                "status", "CANCELLED"));
    }

    @GetMapping("/bookings/{bookingId}/ticket")
    public ResponseEntity<ApiResponse<BusTicketResponse>> getTicket(@PathVariable Long bookingId) {
        return ResponseEntity.ok(ApiResponse.success(ticketService.getTicketDetails(bookingId, currentUserId())));
    }

    private String currentUserId() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return jwt.getSubject();
    }

    private ResponseEntity<?> confirmWithPaymentMethod(ConfirmBookingRequest req) {
        String userId = currentUserId();

        List<SeatBooking> bookings = bookingService.confirmBookingWithPaymentMethod(
                req.getBookingRef(),
                userId,
                req.getPaymentMethod());

        return ResponseEntity.ok(toBookingPaymentResponse(req.getBookingRef(), bookings));
    }

    private Map<String, Object> toBookingPaymentResponse(String bookingRef, List<SeatBooking> bookings) {
        SeatBooking firstBooking = bookings.get(0);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("bookingRef", bookingRef);
        response.put("paymentMethod", firstBooking.getPaymentMethod());
        response.put("paymentRef", firstBooking.getWalletPaymentRef());
        response.put("scheduleId", firstBooking.getSchedule().getId());
        response.put("totalSeats", bookings.size());
        response.put("seats", bookings.stream().map(b -> Map.of(
                "bookingId", b.getId(),
                "seatNumber", b.getSeatNumber(),
                "seatLabel", b.getSeatLabel(),
                "status", b.getStatus())).toList());
        return response;
    }

}
