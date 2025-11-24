package com.goBhutan.adminPanel.busAdmin.controller;

import com.goBhutan.adminPanel.busAdmin.dto.ConfirmBookingRequest;
import com.goBhutan.adminPanel.busAdmin.dto.LockSeatRequest;
import com.goBhutan.adminPanel.busAdmin.entity.Bus;
import com.goBhutan.adminPanel.busAdmin.entity.BusSeatConfig;
import com.goBhutan.adminPanel.busAdmin.entity.Schedule;
import com.goBhutan.adminPanel.busAdmin.entity.SeatBooking;
import com.goBhutan.adminPanel.busAdmin.enums.BookingStatus;
import com.goBhutan.adminPanel.busAdmin.repository.BusScheduleRepository;
import com.goBhutan.adminPanel.busAdmin.repository.SeatBookingRepository;
import com.goBhutan.adminPanel.busAdmin.service.BusBookingService;
import com.goBhutan.adminPanel.busAdmin.service.TicketService;
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
    private final TicketService ticketService;

    @PostMapping("/lock")
    public ResponseEntity<?> lock(@RequestBody LockSeatRequest req) {

        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = jwt.getSubject();

        List<SeatBooking> bookings = bookingService.lockSeats(
                req.getScheduleId(),
                req.getSeatNumbers(),
                req.getSeatLabels(),
                userId,
                req.getApplicantCid(),
                req.getApplicantMobile(),
                req.getApplicantEmail()
        );

        return ResponseEntity.ok(Map.of(
                "paymentRef", bookings.get(0).getPaymentRef(),
                "seatLabel", bookings.get(0).getSeatLabel(),
                "expiresAt", bookings.get(0).getLockExpiry(),
                "seats", bookings
        ));
    }


    @PostMapping("/confirm")
    public ResponseEntity<?> confirm(@RequestBody ConfirmBookingRequest req) {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = jwt.getSubject();

        List<SeatBooking> bookings = bookingService.confirmBooking(req.getPaymentRef(), userId);

        return ResponseEntity.ok(Map.of(
                "paymentRef", req.getPaymentRef(),
                "scheduleId", bookings.get(0).getSchedule().getId(),
                "totalSeats", bookings.size(),
                "seats", bookings.stream().map(b -> Map.of(
                        "bookingId", b.getId(),
                        "seatNumber", b.getSeatNumber(),
                        "seatLabel", b.getSeatLabel(),
                        "status", b.getStatus()
                )).toList()
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

        // All booked/locked
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
            String seatLabel = getSeatLabel(bus, i);

            seats.add(Map.of(
                    "seatNumber", i,
                    "seatLabel", seatLabel,
                    "status", status
            ));
        }

        return ResponseEntity.ok(seats);
    }

    private String getSeatLabel(Bus bus, int seatNumber) {
        for (BusSeatConfig config : bus.getSeatConfigs()) {
            if (seatNumber >= config.getStartNo() && seatNumber <= config.getEndNo()) {
                return config.getSeatLabel();
            }
        }
        return "Seat " + seatNumber;
    }

    @GetMapping("/ticket/{bookingId}")
    public ResponseEntity<byte[]> getTicket(@PathVariable Long bookingId) throws Exception {

        byte[] pdf = ticketService.generateTicket(bookingId);

        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "attachment; filename=ticket-" + bookingId + ".pdf")
                .body(pdf);
    }

    @GetMapping("/admin/schedule/{id}/manifest")
    public ResponseEntity<?> getManifest(@PathVariable Long id) {

        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String adminUserId = jwt.getSubject();

        return ResponseEntity.ok(bookingService.getManifestForSchedule(id, adminUserId));
    }



}
