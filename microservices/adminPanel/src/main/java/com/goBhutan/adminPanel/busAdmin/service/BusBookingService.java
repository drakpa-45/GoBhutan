package com.goBhutan.adminPanel.busAdmin.service;

import com.goBhutan.adminPanel.busAdmin.dto.ManifestItem;
import com.goBhutan.adminPanel.busAdmin.entity.Bus;
import com.goBhutan.adminPanel.busAdmin.entity.BusRoute;
import com.goBhutan.adminPanel.busAdmin.entity.BusSeatConfig;
import com.goBhutan.adminPanel.busAdmin.entity.Schedule;
import com.goBhutan.adminPanel.busAdmin.entity.SeatBooking;
import com.goBhutan.adminPanel.busAdmin.enums.BookingStatus;
import com.goBhutan.adminPanel.busAdmin.repository.BusScheduleRepository;
import com.goBhutan.adminPanel.busAdmin.repository.SeatBookingRepository;
import com.goBhutan.adminPanel.common.entity.AppUser;
import com.goBhutan.adminPanel.common.service.AppUserService;
import com.goBhutan.adminPanel.paymentInt.dto.WalletPaymentRequest;
import com.goBhutan.adminPanel.paymentInt.dto.WalletPaymentResult;
import com.goBhutan.adminPanel.paymentInt.service.PaymentIntegrationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
@Service
@Transactional
@RequiredArgsConstructor
public class BusBookingService {

    private static final String APP_OWNER_USERNAME = "YAYAOWNER";
    private static final String PAYMENT_METHOD_WALLET = "WALLET";
    private static final String PAYMENT_METHOD_CASH = "CASH";
    private static final String BUS_BOOKING_REFERENCE_TYPE = "BUS_BOOKING";
    private static final String BUS_APP_CHARGE_REFERENCE_TYPE = "BUS_APP_CHARGE";

    private final BusScheduleRepository scheduleRepo;
    private final SeatBookingRepository bookingRepo;
    private final SeatBroadcastService broadcastService;
    private final PaymentIntegrationService paymentService;
    private final AppUserService appUserService;

    @Value("${app.clients.bus.seat-lock-minutes:5}")
    private long seatLockMinutes;


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
        ensureScheduleBookable(schedule);
        Bus bus = schedule.getBus();

        if (schedule.getDepartureTime() != null && !schedule.getDepartureTime().isAfter(now)) {
            throw new RuntimeException("Cannot book seats for a departed schedule");
        }

        // Step 3 — Single booking ref for single/multiple seats
        BigDecimal baseFare = getBaseFare(schedule);
        BigDecimal appCharges = getAppCharges(schedule);
        BigDecimal finalFare = getScheduleFare(schedule);

        String bookingRef = UUID.randomUUID().toString();
        LocalDateTime lockExpiry = getLockExpiry(now);

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
            booking.setPaymentMethod(null);
            booking.setStatus(BookingStatus.LOCKED);
            booking.setLockExpiry(lockExpiry);
            applyFareSnapshot(booking, baseFare, appCharges, finalFare);

            bookingList.add(booking);
        }

        List<SeatBooking> saved = bookingRepo.saveAll(bookingList);

        // Step 5 — WebSocket update
        broadcastService.broadcastSeatUpdate(scheduleId);

        return saved;
    }

    @Transactional
    public List<SeatBooking> lockCashSeats(
            Long scheduleId,
            List<Integer> seatNumbers,
            List<String> seatLabels,
            String adminUserId,
            String cid,
            String mobile,
            String email
    ) {

        if (seatNumbers == null || seatNumbers.isEmpty())
            throw new RuntimeException("No seats selected");

        if (new HashSet<>(seatNumbers).size() != seatNumbers.size())
            throw new RuntimeException("Duplicate seat numbers selected");

        LocalDateTime now = LocalDateTime.now();

        bookingRepo.releaseExpiredLocks(now);

        Schedule schedule = scheduleRepo.lockSchedule(scheduleId);
        ensureScheduleBookable(schedule);
        ensureScheduleOwnedByAdmin(schedule, adminUserId);
        Bus bus = schedule.getBus();

        if (schedule.getDepartureTime() != null && !schedule.getDepartureTime().isAfter(now)) {
            throw new RuntimeException("Cannot book seats for a departed schedule");
        }

        String bookingRef = UUID.randomUUID().toString();
        LocalDateTime lockExpiry = getLockExpiry(now);

        List<SeatBooking> bookingList = new ArrayList<>();

        for (Integer seatNumber : seatNumbers) {
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
            booking.setUserId(adminUserId);
            booking.setBookingRef(bookingRef);
            booking.setWalletPaymentRef(null);
            booking.setPaymentMethod(PAYMENT_METHOD_CASH);
            booking.setStatus(BookingStatus.LOCKED);
            booking.setLockExpiry(lockExpiry);
            clearFareSnapshot(booking);

            bookingList.add(booking);
        }

        List<SeatBooking> saved = bookingRepo.saveAll(bookingList);

        broadcastService.broadcastSeatUpdate(scheduleId);

        return saved;
    }

    public List<Map<String, Object>> getSeatStatus(Long scheduleId) {
        Schedule schedule = scheduleRepo.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));
        ensureScheduleBookable(schedule);

        LocalDateTime now = LocalDateTime.now();
        if (schedule.getDepartureTime() != null && !schedule.getDepartureTime().isAfter(now)) {
            throw new RuntimeException("Cannot view seats for a departed schedule");
        }

        Bus bus = schedule.getBus();
        List<SeatBooking> bookings = bookingRepo.findByScheduleId(scheduleId);
        Map<Integer, SeatBooking> bookingMap = bookings.stream()
                .collect(Collectors.toMap(SeatBooking::getSeatNumber, b -> b));

        List<Map<String, Object>> seats = new ArrayList<>();
        for (int i = 1; i <= bus.getTotalSeats(); i++) {
            SeatBooking booking = bookingMap.get(i);

            String status = "AVAILABLE";
            if (booking != null) {
                if (booking.getStatus() == BookingStatus.BOOKED) {
                    status = "BOOKED";
                } else if (booking.getStatus() == BookingStatus.LOCKED
                        && booking.getLockExpiry() != null
                        && booking.getLockExpiry().isAfter(now)) {
                    status = "LOCKED";
                }
            }

            seats.add(Map.of(
                    "seatNumber", i,
                    "seatLabel", getSeatLabel(bus, i),
                    "status", status));
        }

        return seats;
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
        ensureScheduleBookable(schedule);

        if (schedule.getDepartureTime() != null && !schedule.getDepartureTime().isAfter(now)) {
            throw new RuntimeException("Cannot confirm seats for a departed schedule");
        }

        ensureBookingFareSnapshots(bookings, schedule);
        BigDecimal totalAmount = sumFinalFare(bookings);
        BigDecimal ownerSettlementAmount = sumBaseFare(bookings);
        BigDecimal appChargeSettlementAmount = sumAppCharges(bookings);

        if (bookings.stream().anyMatch(b -> b.getWalletPaymentRef() != null && !b.getWalletPaymentRef().isBlank()))
            throw new RuntimeException("Wallet payment already processed for this booking");

        WalletPaymentRequest paymentRequest = new WalletPaymentRequest();
        paymentRequest.setAmount(totalAmount);
        paymentRequest.setCurrency("BTN");
        paymentRequest.setServiceName("BUS");
        paymentRequest.setReferenceType(BUS_BOOKING_REFERENCE_TYPE);
        paymentRequest.setReferenceId(bookingRef);
        paymentRequest.setDescription("Bus booking payment");

        WalletPaymentResult walletPayment = paymentService.payWithWallet(paymentRequest, userId);

        if (ownerSettlementAmount.compareTo(BigDecimal.ZERO) > 0) {
            paymentService.creditServiceSettlement(
                    walletPayment.getPaymentRef(),
                    ownerSettlementAmount,
                    "BUS",
                    BUS_BOOKING_REFERENCE_TYPE,
                    bookingRef,
                    "Bus booking fare settlement",
                    schedule.getBus().getAdminUserId()
            );
        }

        if (appChargeSettlementAmount.compareTo(BigDecimal.ZERO) > 0) {
            paymentService.creditServiceSettlement(
                    walletPayment.getPaymentRef(),
                    appChargeSettlementAmount,
                    "BUS",
                    BUS_APP_CHARGE_REFERENCE_TYPE,
                    bookingRef,
                    "Bus booking app charge settlement",
                    getAppOwnerUserId()
            );
        }

        // Confirm seats
        for (SeatBooking b : bookings) {
            b.setWalletPaymentRef(walletPayment.getPaymentRef());
            b.setPaymentMethod(PAYMENT_METHOD_WALLET);
            b.setStatus(BookingStatus.BOOKED);
            b.setLockExpiry(null);
        }

        // Reduce seats count
        schedule.setAvailableSeats(schedule.getAvailableSeats() - bookings.size());

        List<SeatBooking> saved = bookingRepo.saveAll(bookings);

        broadcastService.broadcastSeatUpdate(schedule.getId());

        return saved;
    }

    @Transactional
    public List<SeatBooking> confirmCashBooking(String bookingRef, String adminUserId) {

        List<SeatBooking> bookings = bookingRepo.findByBookingRefForUpdate(bookingRef);

        if (bookings.isEmpty())
            throw new RuntimeException("Invalid or expired booking reference");

        if (bookings.stream().anyMatch(b -> !matchesCurrentAdmin(b.getUserId(), adminUserId)))
            throw new RuntimeException("Unauthorized confirmation attempt");

        LocalDateTime now = LocalDateTime.now();

        if (bookings.stream().anyMatch(b -> b.getStatus() != BookingStatus.LOCKED))
            throw new RuntimeException("Booking is no longer pending confirmation");

        if (bookings.stream().anyMatch(b -> b.getLockExpiry() == null || b.getLockExpiry().isBefore(now)))
            throw new RuntimeException("Seat lock expired");

        if (bookings.stream().anyMatch(b -> !PAYMENT_METHOD_CASH.equalsIgnoreCase(b.getPaymentMethod())))
            throw new RuntimeException("Cash confirmation is only allowed for cash-locked bookings");

        Schedule schedule = scheduleRepo.lockSchedule(bookings.get(0).getSchedule().getId());
        ensureScheduleBookable(schedule);
        ensureScheduleOwnedByAdmin(schedule, adminUserId);

        if (schedule.getDepartureTime() != null && !schedule.getDepartureTime().isAfter(now)) {
            throw new RuntimeException("Cannot confirm seats for a departed schedule");
        }

        for (SeatBooking b : bookings) {
            b.setWalletPaymentRef(null);
            b.setPaymentMethod(PAYMENT_METHOD_CASH);
            b.setStatus(BookingStatus.BOOKED);
            b.setLockExpiry(null);
            clearFareSnapshot(b);
        }

        decreaseAvailableSeats(schedule, bookings.size());

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
            if (PAYMENT_METHOD_CASH.equalsIgnoreCase(targetBooking.getPaymentMethod())) {
                increaseAvailableSeats(schedule, 1);
                targetBooking.setStatus(BookingStatus.CANCELLED);
                targetBooking.setLockExpiry(null);

                SeatBooking savedBooking = bookingRepo.save(targetBooking);
                broadcastService.broadcastSeatUpdate(schedule.getId());

                return savedBooking;
            }

            if (walletPaymentRef == null || walletPaymentRef.isBlank()) {
                throw new RuntimeException("Missing wallet payment reference for booked seat");
            }

            long paidSeatCount = bookings.stream()
                    .filter(b -> walletPaymentRef.equals(b.getWalletPaymentRef()))
                    .count();

            if (paidSeatCount <= 0) {
                throw new RuntimeException("No paid seats found for cancellation");
            }

            BigDecimal seatCount = BigDecimal.valueOf(paidSeatCount);
            BigDecimal originalPaidAmount = paymentService.getSuccessfulServicePaymentAmount(walletPaymentRef, userId);
            BigDecimal refundAmount = fareOrFallback(targetBooking.getFinalFareAtBooking(),
                    originalPaidAmount.divide(seatCount, 2, RoundingMode.HALF_UP));
            BigDecimal ownerReversalAmount = fareOrFallback(targetBooking.getBaseFareAtBooking(),
                    paymentService.getSuccessfulSettlementAmount(walletPaymentRef, BUS_BOOKING_REFERENCE_TYPE)
                            .divide(seatCount, 2, RoundingMode.HALF_UP));
            BigDecimal appChargeReversalAmount = fareOrFallback(targetBooking.getAppChargesAtBooking(),
                    paymentService.getSuccessfulSettlementAmount(walletPaymentRef, BUS_APP_CHARGE_REFERENCE_TYPE)
                            .divide(seatCount, 2, RoundingMode.HALF_UP));

            if (ownerReversalAmount.compareTo(BigDecimal.ZERO) > 0) {
                paymentService.reverseServiceSettlement(
                        walletPaymentRef,
                        ownerReversalAmount,
                        "BUS",
                        BUS_BOOKING_REFERENCE_TYPE,
                        targetBooking.getBookingRef(),
                        "Bus booking fare settlement reversal",
                        schedule.getBus().getAdminUserId()
                );
            }

            if (appChargeReversalAmount.compareTo(BigDecimal.ZERO) > 0) {
                paymentService.reverseServiceSettlement(
                        walletPaymentRef,
                        appChargeReversalAmount,
                        "BUS",
                        BUS_APP_CHARGE_REFERENCE_TYPE,
                        targetBooking.getBookingRef(),
                        "Bus booking app charge settlement reversal",
                        getAppOwnerUserId()
                );
            }

            paymentService.refundToWallet(
                    walletPaymentRef,
                    refundAmount,
                    "BUS",
                    BUS_BOOKING_REFERENCE_TYPE,
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

    public SeatBooking cancelCashByAdmin(Long bookingId, String adminUserId) {
        SeatBooking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!PAYMENT_METHOD_CASH.equalsIgnoreCase(booking.getPaymentMethod())) {
            throw new RuntimeException("Booking is not a cash booking");
        }

        Schedule schedule = scheduleRepo.lockSchedule(booking.getSchedule().getId());
        ensureScheduleOwnedByAdmin(schedule, adminUserId);

        if (booking.getStatus() == BookingStatus.CANCELLED)
            throw new RuntimeException("Booking already cancelled");

        if (booking.getStatus() == BookingStatus.EXPIRED)
            throw new RuntimeException("Booking already expired");

        if (booking.getStatus() == BookingStatus.BOOKED) {
            increaseAvailableSeats(schedule, 1);
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setLockExpiry(null);

        SeatBooking savedBooking = bookingRepo.save(booking);
        broadcastService.broadcastSeatUpdate(schedule.getId());

        return savedBooking;
    }

    public List<ManifestItem> getManifestForSchedule(Long scheduleId, String adminUserId) {

        Schedule sched = scheduleRepo.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));

        if (!matchesCurrentAdmin(sched.getBus().getAdminUserId(), adminUserId))
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

    private void ensureScheduleBookable(Schedule schedule) {
        if (schedule == null) {
            throw new RuntimeException("Schedule not found");
        }
        if (!Boolean.TRUE.equals(schedule.getActive())) {
            throw new RuntimeException("Schedule is not active");
        }
        Bus bus = schedule.getBus();
        if (bus == null || Boolean.FALSE.equals(bus.getIsActive())) {
            throw new RuntimeException("Bus is not active");
        }
        BusRoute route = schedule.getRoute();
        if (route == null || !Boolean.TRUE.equals(route.getActive())) {
            throw new RuntimeException("Route is not active");
        }
    }

    private void ensureScheduleOwnedByAdmin(Schedule schedule, String adminUserId) {
        Bus bus = schedule.getBus();
        if (bus == null || !matchesCurrentAdmin(bus.getAdminUserId(), adminUserId)) {
            throw new RuntimeException("Unauthorized");
        }
    }

    private LocalDateTime getLockExpiry(LocalDateTime now) {
        if (seatLockMinutes <= 0) {
            throw new RuntimeException("Seat lock duration must be greater than zero");
        }
        return now.plusMinutes(seatLockMinutes);
    }

    private boolean matchesCurrentAdmin(String storedUserId, String currentKeycloakId) {
        if (isBlank(storedUserId) || isBlank(currentKeycloakId)) {
            return false;
        }
        if (storedUserId.equals(currentKeycloakId)) {
            return true;
        }

        boolean storedValueIsUsernameForCurrentUser = appUserService.findByUsername(storedUserId)
                .map(AppUser::getKeycloakId)
                .filter(currentKeycloakId::equals)
                .isPresent();
        if (storedValueIsUsernameForCurrentUser) {
            return true;
        }

        return appUserService.findByKeycloakId(currentKeycloakId)
                .map(AppUser::getUsername)
                .filter(storedUserId::equals)
                .isPresent();
    }

    private void decreaseAvailableSeats(Schedule schedule, int bookedSeatCount) {
        int currentAvailableSeats = currentAvailableSeats(schedule);
        if (currentAvailableSeats < bookedSeatCount) {
            throw new RuntimeException("Not enough seats available");
        }
        schedule.setAvailableSeats(currentAvailableSeats - bookedSeatCount);
    }

    private void increaseAvailableSeats(Schedule schedule, int releasedSeatCount) {
        int currentAvailableSeats = currentAvailableSeats(schedule);
        int totalSeats = schedule.getBus() == null || schedule.getBus().getTotalSeats() == null
                ? currentAvailableSeats + releasedSeatCount
                : schedule.getBus().getTotalSeats();
        schedule.setAvailableSeats(Math.min(totalSeats, currentAvailableSeats + releasedSeatCount));
    }

    private int currentAvailableSeats(Schedule schedule) {
        if (schedule.getAvailableSeats() != null) {
            return schedule.getAvailableSeats();
        }
        if (schedule.getBus() == null || schedule.getBus().getTotalSeats() == null) {
            throw new RuntimeException("Schedule available seats is not configured");
        }
        long bookedSeats = bookingRepo.countByScheduleIdAndStatus(schedule.getId(), BookingStatus.BOOKED);
        return schedule.getBus().getTotalSeats() - (int) bookedSeats;
    }

    private BigDecimal getScheduleFare(Schedule schedule) {
        if (schedule == null || schedule.getFinalFare() == null) {
            throw new RuntimeException("Schedule fare is not configured");
        }
        return money(schedule.getFinalFare());
    }

    private BigDecimal getBaseFare(Schedule schedule) {
        if (schedule == null || schedule.getBaseFare() == null) {
            throw new RuntimeException("Schedule base fare is not configured");
        }
        return money(schedule.getBaseFare());
    }

    private BigDecimal getAppCharges(Schedule schedule) {
        if (schedule == null) {
            throw new RuntimeException("Schedule route is not configured");
        }
        return money(schedule.getAppCharges());
    }

    private void ensureBookingFareSnapshots(List<SeatBooking> bookings, Schedule schedule) {
        BigDecimal baseFare = getBaseFare(schedule);
        BigDecimal appCharges = getAppCharges(schedule);
        BigDecimal finalFare = getScheduleFare(schedule);

        for (SeatBooking booking : bookings) {
            if (booking.getBaseFareAtBooking() == null
                    || booking.getAppChargesAtBooking() == null
                    || booking.getFinalFareAtBooking() == null) {
                applyFareSnapshot(booking, baseFare, appCharges, finalFare);
            }
        }
    }

    private void applyFareSnapshot(SeatBooking booking, BigDecimal baseFare, BigDecimal appCharges, BigDecimal finalFare) {
        booking.setBaseFareAtBooking(money(baseFare));
        booking.setAppChargesAtBooking(money(appCharges));
        booking.setFinalFareAtBooking(money(finalFare));
    }

    private void clearFareSnapshot(SeatBooking booking) {
        booking.setBaseFareAtBooking(null);
        booking.setAppChargesAtBooking(null);
        booking.setFinalFareAtBooking(null);
    }

    private BigDecimal sumFinalFare(List<SeatBooking> bookings) {
        return bookings.stream()
                .map(SeatBooking::getFinalFareAtBooking)
                .map(this::money)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumBaseFare(List<SeatBooking> bookings) {
        return bookings.stream()
                .map(SeatBooking::getBaseFareAtBooking)
                .map(this::money)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumAppCharges(List<SeatBooking> bookings) {
        return bookings.stream()
                .map(SeatBooking::getAppChargesAtBooking)
                .map(this::money)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal fareOrFallback(BigDecimal fare, BigDecimal fallback) {
        return fare != null ? money(fare) : money(fallback);
    }

    private BigDecimal money(BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String getAppOwnerUserId() {
        AppUser appOwner = appUserService.findByUsername(APP_OWNER_USERNAME)
                .orElseThrow(() -> new RuntimeException("App owner user not found: " + APP_OWNER_USERNAME));
        if (appOwner.getKeycloakId() == null || appOwner.getKeycloakId().isBlank()) {
            throw new RuntimeException("App owner Keycloak ID is not configured: " + APP_OWNER_USERNAME);
        }
        return appOwner.getKeycloakId();
    }

}
