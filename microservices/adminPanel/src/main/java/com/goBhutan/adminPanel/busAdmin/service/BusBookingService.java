package com.goBhutan.adminPanel.busAdmin.service;

import com.goBhutan.adminPanel.busAdmin.dto.ManifestItem;
import com.goBhutan.adminPanel.busAdmin.entity.Bus;
import com.goBhutan.adminPanel.busAdmin.entity.BusSeatConfig;
import com.goBhutan.adminPanel.busAdmin.entity.Schedule;
import com.goBhutan.adminPanel.busAdmin.entity.SeatBooking;
import com.goBhutan.adminPanel.busAdmin.enums.BookingStatus;
import com.goBhutan.adminPanel.busAdmin.repository.BusScheduleRepository;
import com.goBhutan.adminPanel.busAdmin.repository.SeatBookingRepository;
import com.goBhutan.adminPanel.paymentInt.dto.WalletPaymentRequest;
import com.goBhutan.adminPanel.paymentInt.dto.WalletPaymentResult;
import com.goBhutan.adminPanel.paymentInt.service.PaymentIntegrationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
@Service
@Transactional
@RequiredArgsConstructor
public class BusBookingService {

    private final BusScheduleRepository scheduleRepo;
    private final SeatBookingRepository bookingRepo;
    private final SeatBroadcastService broadcastService;
    private final PaymentIntegrationService paymentService;


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

        if (new HashSet<>(seatNumbers).size() != seatNumbers.size())
            throw new RuntimeException("Duplicate seat numbers selected");

        LocalDateTime now = LocalDateTime.now();

        // Release expired locks
        bookingRepo.releaseExpiredLocks(now);

        // Step 2 — Lock parent schedule
        Schedule schedule = scheduleRepo.lockSchedule(scheduleId);
        Bus bus = schedule.getBus();

        if (Boolean.FALSE.equals(schedule.getActive())) {
            throw new RuntimeException("Schedule is not active");
        }

        if (schedule.getDepartureTime() != null && !schedule.getDepartureTime().isAfter(now)) {
            throw new RuntimeException("Cannot book seats for a departed schedule");
        }

        // Step 3 — Single booking ref for single/multiple seats
        String bookingRef = UUID.randomUUID().toString();
        LocalDateTime lockExpiry = now.plusMinutes(5);

        List<SeatBooking> bookingList = new ArrayList<>();

        // Step 4 — Reuse the same seat row for cancelled/expired seats
        for (int i = 0; i < seatNumbers.size(); i++) {
            Integer seatNumber = seatNumbers.get(i);
            validateSeatNumber(seatNumber, bus.getTotalSeats());
            String seatLabel = getSeatLabel(bus, seatNumber);

            SeatBooking booking = bookingRepo.findByScheduleIdAndSeatNumber(scheduleId, seatNumber)
                    .orElseGet(() -> {
                        SeatBooking freshBooking = new SeatBooking();
                        freshBooking.setSchedule(schedule);
                        freshBooking.setSeatNumber(seatNumber);
                        return freshBooking;
                    });

            if (booking.getId() != null) {
                if (booking.getStatus() == BookingStatus.BOOKED) {
                    throw new RuntimeException("Seat " + seatNumber + " is already booked");
                }

                if (booking.getStatus() == BookingStatus.LOCKED
                        && booking.getLockExpiry() != null
                        && booking.getLockExpiry().isAfter(now)) {
                    throw new RuntimeException("Seat " + seatNumber + " is already locked");
                }
            }

            booking.setSeatLabel(seatLabel);
            booking.setApplicantCid(cid);
            booking.setApplicantMobile(mobile);
            booking.setApplicantEmail(email);

            booking.setUserId(userId);
            booking.setBookingRef(bookingRef);
            booking.setWalletPaymentRef(null);
            booking.setStatus(BookingStatus.LOCKED);
            booking.setLockExpiry(lockExpiry);

            bookingList.add(booking);
        }

        List<SeatBooking> saved = bookingRepo.saveAll(bookingList);

        // Step 5 — WebSocket update
        broadcastService.broadcastSeatUpdate(scheduleId);

        return saved;
    }


    @Transactional
    public List<SeatBooking> confirmBooking(String bookingRef, String userId) {

        List<SeatBooking> bookings = bookingRepo.findByBookingRefForUpdate(bookingRef);

        if (bookings.isEmpty())
            throw new RuntimeException("Invalid or expired booking reference");

        // Validate same user
        if (bookings.stream().anyMatch(b -> !b.getUserId().equals(userId)))
            throw new RuntimeException("Unauthorized confirmation attempt");

        LocalDateTime now = LocalDateTime.now();

        if (bookings.stream().anyMatch(b -> b.getStatus() != BookingStatus.LOCKED))
            throw new RuntimeException("Booking is no longer pending confirmation");

        if (bookings.stream().anyMatch(b -> b.getLockExpiry() == null || b.getLockExpiry().isBefore(now)))
            throw new RuntimeException("Seat lock expired");

        Schedule schedule = scheduleRepo.lockSchedule(bookings.get(0).getSchedule().getId());

        if (Boolean.FALSE.equals(schedule.getActive())) {
            throw new RuntimeException("Schedule is not active");
        }

        if (schedule.getDepartureTime() != null && !schedule.getDepartureTime().isAfter(now)) {
            throw new RuntimeException("Cannot confirm seats for a departed schedule");
        }

        BigDecimal totalAmount = schedule.getPrice().multiply(BigDecimal.valueOf(bookings.size()));

        if (bookings.stream().anyMatch(b -> b.getWalletPaymentRef() != null && !b.getWalletPaymentRef().isBlank()))
            throw new RuntimeException("Wallet payment already processed for this booking");

        WalletPaymentRequest paymentRequest = new WalletPaymentRequest();
        paymentRequest.setAmount(totalAmount);
        paymentRequest.setCurrency("BTN");
        paymentRequest.setServiceName("BUS");
        paymentRequest.setReferenceType("BUS_BOOKING");
        paymentRequest.setReferenceId(bookingRef);
        paymentRequest.setDescription("Bus booking payment");

        WalletPaymentResult walletPayment = paymentService.payWithWallet(paymentRequest, userId);

        paymentService.creditServiceSettlement(
                walletPayment.getPaymentRef(),
                totalAmount,
                "BUS",
                "BUS_BOOKING",
                bookingRef,
                "Bus booking settlement",
                schedule.getBus().getAdminUserId()
        );

        // Confirm seats
        for (SeatBooking b : bookings) {
            b.setWalletPaymentRef(walletPayment.getPaymentRef());
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

        List<SeatBooking> bookings = (booking.getBookingRef() != null && !booking.getBookingRef().isBlank())
                ? bookingRepo.findByBookingRefForUpdate(booking.getBookingRef())
                : List.of(booking);

        SeatBooking targetBooking = bookings.stream()
                .filter(b -> b.getId().equals(bookingId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (bookings.stream().anyMatch(b -> !b.getUserId().equals(userId)))
            throw new RuntimeException("Unauthorized cancellation");

        if (targetBooking.getStatus() == BookingStatus.CANCELLED)
            throw new RuntimeException("Booking already cancelled");

        if (targetBooking.getStatus() == BookingStatus.EXPIRED)
            throw new RuntimeException("Booking already expired");

        Schedule schedule = scheduleRepo.lockSchedule(targetBooking.getSchedule().getId());

        if (targetBooking.getStatus() == BookingStatus.BOOKED) {
            String walletPaymentRef = targetBooking.getWalletPaymentRef();
            if (walletPaymentRef == null || walletPaymentRef.isBlank()) {
                throw new RuntimeException("Missing wallet payment reference for booked seat");
            }

            long paidSeatCount = bookings.stream()
                    .filter(b -> walletPaymentRef.equals(b.getWalletPaymentRef()))
                    .count();

            if (paidSeatCount <= 0) {
                throw new RuntimeException("No paid seats found for cancellation");
            }

            BigDecimal originalPaidAmount = paymentService.getSuccessfulServicePaymentAmount(walletPaymentRef, userId);
            BigDecimal refundAmount = originalPaidAmount
                    .divide(BigDecimal.valueOf(paidSeatCount), 2, RoundingMode.HALF_UP);

            paymentService.reverseServiceSettlement(
                    walletPaymentRef,
                    refundAmount,
                    "BUS",
                    "BUS_BOOKING",
                    targetBooking.getBookingRef(),
                    "Bus booking settlement reversal",
                    schedule.getBus().getAdminUserId()
            );

            paymentService.refundToWallet(
                    walletPaymentRef,
                    refundAmount,
                    "BUS",
                    "BUS_BOOKING",
                    targetBooking.getBookingRef(),
                    "Bus booking refund",
                    userId
            );

            schedule.setAvailableSeats(schedule.getAvailableSeats() + 1);
        }

        targetBooking.setStatus(BookingStatus.CANCELLED);
        targetBooking.setLockExpiry(null);

        SeatBooking savedBooking = bookingRepo.save(targetBooking);
        broadcastService.broadcastSeatUpdate(schedule.getId());

        return savedBooking;
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

    private void validateSeatNumber(Integer seatNumber, Integer totalSeats) {
        if (seatNumber == null || seatNumber < 1 || totalSeats == null || seatNumber > totalSeats) {
            throw new RuntimeException("Invalid seat number: " + seatNumber);
        }
    }

}
