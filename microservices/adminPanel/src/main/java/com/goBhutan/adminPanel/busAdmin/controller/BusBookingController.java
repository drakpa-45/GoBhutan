package com.goBhutan.adminPanel.busAdmin.controller;

import com.goBhutan.adminPanel.busAdmin.dto.ConfirmBookingRequest;
import com.goBhutan.adminPanel.busAdmin.dto.LockSeatRequest;
import com.goBhutan.adminPanel.busAdmin.entity.Bus;
import com.goBhutan.adminPanel.busAdmin.entity.Schedule;
import com.goBhutan.adminPanel.busAdmin.entity.SeatBooking;
import com.goBhutan.adminPanel.busAdmin.enums.BookingStatus;
import com.goBhutan.adminPanel.busAdmin.repository.BusScheduleRepository;
import com.goBhutan.adminPanel.busAdmin.repository.SeatBookingRepository;
import com.goBhutan.adminPanel.busAdmin.service.BusBookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BusBookingController {

    private final BusBookingService bookingService;
    private final SeatBookingRepository bookingRepo;
    private final BusScheduleRepository scheduleRepo;

    @PostMapping("/lock")
    public ResponseEntity<?> lock(@RequestBody LockSeatRequest req) {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = jwt.getSubject();

        SeatBooking booking = bookingService.lockSeat(req.getScheduleId(), req.getSeatNumber(), userId);

        return ResponseEntity.ok(Map.of(
                "seat", booking.getSeatNumber(),
                "expiresAt", booking.getLockExpiry(),
                "paymentRef", booking.getPaymentRef(),
                "status", booking.getStatus()
        ));
    }


    @PostMapping("/confirm")
    public ResponseEntity<?> confirm(@RequestBody ConfirmBookingRequest req) {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = jwt.getSubject();

        SeatBooking booking = bookingService.confirmBooking(req.getPaymentRef(), userId);

        return ResponseEntity.ok(Map.of(
                "bookingId", booking.getId(),
                "schedule", booking.getSchedule().getId(),
                "seat", booking.getSeatNumber(),
                "status", booking.getStatus()
        ));
    }


    @PostMapping("/cancel/{bookingId}")
    public ResponseEntity<?> cancel(@PathVariable Long bookingId) {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = jwt.getSubject();

        SeatBooking booking = bookingService.cancel(bookingId, userId);

        return ResponseEntity.ok(Map.of(
                "bookingId", booking.getId(),
                "status", "CANCELLED"
        ));
    }

    @GetMapping("/schedule/{scheduleId}/seats")
    public ResponseEntity<?> getSeatStatus(@PathVariable Long scheduleId) {

        Schedule schedule = scheduleRepo.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));

        Bus bus = schedule.getBus();

        // Create seat layout based on BusSeatConfig
        List<Map<String, Object>> seats = new ArrayList<>();

        // All booked/locked seats
        List<SeatBooking> bookings = bookingRepo.findByScheduleId(scheduleId);
        Map<Integer, SeatBooking> bookingMap = bookings.stream()
                .collect(Collectors.toMap(SeatBooking::getSeatNumber, b -> b));

        // Build seat map
        for (int i = 1; i <= bus.getTotalSeats(); i++) {
            SeatBooking b = bookingMap.get(i);

            String status = "AVAILABLE";
            if (b != null) {
                if (b.getStatus() == BookingStatus.BOOKED)
                    status = "BOOKED";
                else if (b.getStatus() == BookingStatus.LOCKED && b.getLockExpiry().isAfter(LocalDateTime.now()))
                    status = "LOCKED";
            }

            seats.add(Map.of(
                    "seatNumber", i,
                    "status", status
            ));
        }

        return ResponseEntity.ok(seats);
    }

}
