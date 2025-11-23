package com.goBhutan.adminPanel.busAdmin.service;

import com.goBhutan.adminPanel.busAdmin.entity.Schedule;
import com.goBhutan.adminPanel.busAdmin.entity.SeatBooking;
import com.goBhutan.adminPanel.busAdmin.enums.BookingStatus;
import com.goBhutan.adminPanel.busAdmin.repository.BusScheduleRepository;
import com.goBhutan.adminPanel.busAdmin.repository.SeatBookingRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
@Service
@Transactional
@RequiredArgsConstructor
public class BusBookingService {

    private final BusScheduleRepository scheduleRepo;

    private final SeatBookingRepository bookingRepo;



    /**
     * Step 1: LOCK SEAT (5 minute hold)
     */
    public SeatBooking lockSeat(Long scheduleId, int seatNumber, String userId) {

        // Release expired locks
        bookingRepo.releaseExpiredLocks(LocalDateTime.now());

        // Check if seat is taken
        boolean taken = bookingRepo.isSeatTaken(scheduleId, seatNumber, LocalDateTime.now());
        if (taken)
            throw new RuntimeException("Seat already booked or locked.");

        // Lock schedule to prevent race conditions
        Schedule schedule = scheduleRepo.lockSchedule(scheduleId);

        SeatBooking booking = new SeatBooking();
        booking.setSchedule(schedule);
        booking.setSeatNumber(seatNumber);
        booking.setUserId(userId);
        booking.setStatus(BookingStatus.LOCKED);
        booking.setLockExpiry(LocalDateTime.now().plusMinutes(5));
        booking.setPaymentRef(UUID.randomUUID().toString()); // temporary token

        return bookingRepo.save(booking);
    }

    public SeatBooking confirmBooking(String paymentRef, String userId) {

        SeatBooking booking = bookingRepo.findByPaymentRef(paymentRef)
                .orElseThrow(() -> new RuntimeException("Invalid booking reference"));

        if (!booking.getUserId().equals(userId))
            throw new RuntimeException("Unauthorized booking confirmation");

        if (booking.getStatus() != BookingStatus.LOCKED)
            throw new RuntimeException("Booking is not in locked state");

        if (booking.getLockExpiry().isBefore(LocalDateTime.now()))
            throw new RuntimeException("Seat lock has expired");

        // CONVERT LOCK → BOOKED
        booking.setStatus(BookingStatus.BOOKED);
        booking.setLockExpiry(null);

        // Reduce available seats
        Schedule schedule = booking.getSchedule();
        schedule.setAvailableSeats(schedule.getAvailableSeats() - 1);
        scheduleRepo.save(schedule);

        return bookingRepo.save(booking);
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
        scheduleRepo.save(schedule);

        return bookingRepo.save(booking);
    }
}
