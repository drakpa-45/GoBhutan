package com.goBhutan.adminPanel.busAdmin.service;

import com.goBhutan.adminPanel.busAdmin.dto.ManifestItem;
import com.goBhutan.adminPanel.busAdmin.entity.Bus;
import com.goBhutan.adminPanel.busAdmin.entity.BusSeatConfig;
import com.goBhutan.adminPanel.busAdmin.entity.Schedule;
import com.goBhutan.adminPanel.busAdmin.entity.SeatBooking;
import com.goBhutan.adminPanel.busAdmin.enums.BookingStatus;
import com.goBhutan.adminPanel.busAdmin.repository.BusScheduleRepository;
import com.goBhutan.adminPanel.busAdmin.repository.SeatBookingRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
@Service
@Transactional
@RequiredArgsConstructor
public class BusBookingService {

    private final BusScheduleRepository scheduleRepo;
    private final SeatBookingRepository bookingRepo;
    private final SeatBroadcastService broadcastService;


    /**
     * Step 1: LOCK SEAT (5 minute hold)
     */
    @Transactional
    public List<SeatBooking> lockSeats(
            Long scheduleId,
            List<Integer> seatNumbers,
            List<String> seatLabels,
            String userId,
            String cid,
            String mobile,
            String email
    ) {

        if (seatNumbers == null || seatNumbers.isEmpty())
            throw new RuntimeException("No seats selected");

        if (seatLabels == null || seatLabels.size() != seatNumbers.size())
            throw new RuntimeException("Seat numbers and labels mismatch");

        // Release expired locks
        bookingRepo.releaseExpiredLocks(LocalDateTime.now());

        // Step 1 — Check availability for ALL seats
        for (Integer seat : seatNumbers) {
            if (bookingRepo.isSeatTaken(scheduleId, seat, LocalDateTime.now())) {
                throw new RuntimeException("Seat " + seat + " is already booked or locked");
            }
        }

        // Step 2 — Lock parent schedule
        Schedule schedule = scheduleRepo.lockSchedule(scheduleId);

        // Step 3 — Single payment ref for single/multiple seats
        String paymentRef = UUID.randomUUID().toString();

        List<SeatBooking> bookingList = new ArrayList<>();

        // Step 4 — Create multiple booking rows
        for (int i = 0; i < seatNumbers.size(); i++) {
            SeatBooking booking = new SeatBooking();

            booking.setSchedule(schedule);
            booking.setSeatNumber(seatNumbers.get(i));
            booking.setSeatLabel(seatLabels.get(i));
            booking.setApplicantCid(cid);
            booking.setApplicantMobile(mobile);
            booking.setApplicantEmail(email);

            booking.setUserId(userId);
            booking.setPaymentRef(paymentRef);
            booking.setStatus(BookingStatus.LOCKED);
            booking.setLockExpiry(LocalDateTime.now().plusMinutes(5));

            bookingList.add(booking);
        }

        List<SeatBooking> saved = bookingRepo.saveAll(bookingList);

        // Step 5 — WebSocket update
        broadcastService.broadcastSeatUpdate(scheduleId);

        return saved;
    }


    @Transactional
    public List<SeatBooking> confirmBooking(String paymentRef, String userId) {

        List<SeatBooking> bookings = bookingRepo.findByPaymentRef(paymentRef);

        if (bookings.isEmpty())
            throw new RuntimeException("Invalid or expired payment reference");

        // Validate same user
        if (bookings.stream().anyMatch(b -> !b.getUserId().equals(userId)))
            throw new RuntimeException("Unauthorized confirmation attempt");

        // Validate lock
        if (bookings.stream().anyMatch(b -> b.getLockExpiry().isBefore(LocalDateTime.now())))
            throw new RuntimeException("Seat lock expired");

        Schedule schedule = bookings.get(0).getSchedule();

        // Confirm seats
        for (SeatBooking b : bookings) {
            b.setStatus(BookingStatus.BOOKED);
            b.setLockExpiry(null);
        }

        // Reduce seats count
        schedule.setAvailableSeats(schedule.getAvailableSeats() - bookings.size());

        List<SeatBooking> saved = bookingRepo.saveAll(bookings);

        broadcastService.broadcastSeatUpdate(schedule.getId());

        return saved;
    }



    /**
     * Step 3: CANCEL BOOKING (user or admin)
     */
    public SeatBooking cancel(Long bookingId, String userId) {
        SeatBooking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getUserId().equals(userId))
            throw new RuntimeException("Unauthorized cancellation");

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setLockExpiry(null);

        // Restore seat
        Schedule schedule = booking.getSchedule();
        schedule.setAvailableSeats(schedule.getAvailableSeats() + 1);
       // scheduleRepo.save(schedule);

        SeatBooking saved = bookingRepo.save(booking);
        broadcastService.broadcastSeatUpdate(schedule.getId());

        return saved;
    }

    public List<ManifestItem> getManifestForSchedule(Long scheduleId, String adminUserId) {

        Schedule sched = scheduleRepo.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));

        if (!sched.getBus().getAdminUserId().equals(adminUserId))
            throw new RuntimeException("Unauthorized");
        Bus bus = sched.getBus();
        return bookingRepo.findByScheduleId(scheduleId).stream()
                .filter(b -> b.getStatus() == BookingStatus.BOOKED)
                .map(b -> new ManifestItem(
                        b.getSeatNumber(),
                        getSeatLabel(bus, b.getSeatNumber()),
                        b.getApplicantCid(),
                        b.getApplicantMobile(),
                        b.getApplicantEmail(),
                        b.getStatus().name()
                ))
                .sorted(Comparator.comparing(ManifestItem::getSeatNumber))
                .toList();
    }

    private String getSeatLabel(Bus bus, Integer seatNumber) {
        for (BusSeatConfig config : bus.getSeatConfigs()) {
            if (seatNumber >= config.getStartNo() && seatNumber <= config.getEndNo()) {
                return config.getSeatLabel();
            }
        }
        return "Seat " + seatNumber;
    }

}
