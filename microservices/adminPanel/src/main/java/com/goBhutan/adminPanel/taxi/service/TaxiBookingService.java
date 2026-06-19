package com.goBhutan.adminPanel.taxi.service;


import com.goBhutan.adminPanel.taxi.dto.request.BookingRequest;
import com.goBhutan.adminPanel.taxi.dto.response.BookingResponse;
import com.goBhutan.adminPanel.taxi.dto.response.FareBreakdown;
import com.goBhutan.adminPanel.taxi.entity.InterRoute;
import com.goBhutan.adminPanel.taxi.entity.Payment;
import com.goBhutan.adminPanel.taxi.entity.TaxiBooking;
import com.goBhutan.adminPanel.taxi.enums.*;
import com.goBhutan.adminPanel.taxi.repository.InterRouteRepository;
import com.goBhutan.adminPanel.taxi.repository.PaymentRepository;
import com.goBhutan.adminPanel.taxi.repository.TaxiBookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaxiBookingService {

    private final TaxiBookingRepository bookingRepo;
    private final InterRouteRepository routeRepo;
    private final PaymentRepository paymentRepo;
    private final FareCalculatorService   fareCalc;

    // ─────────────────────────────────────────────────────────────────────────
    // CREATE BOOKING — routes to the correct mode handler
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public BookingResponse createBooking(BookingRequest req) {
        validate(req);

        TripCategory cat  = req.getTripCategory();
        TripMode mode = req.getTripMode();

        if (cat == TripCategory.INTRA_DZONGKHAG && mode == TripMode.PULL)
            return handleIntraPull(req);

        if (cat == TripCategory.INTRA_DZONGKHAG && mode == TripMode.RESERVED)
            return handleIntraReserved(req);

        if (cat == TripCategory.INTER_DZONGKHAG && mode == TripMode.PULL)
            return handleInterPull(req);

        if (cat == TripCategory.INTER_DZONGKHAG && mode == TripMode.RESERVED)
            return handleInterReserved(req);

        throw new IllegalArgumentException("Unknown trip combination: " + cat + " + " + mode);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MODE HANDLERS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * INTRA + PULL
     * - Calculate fare by distance
     * - Wallet/mPay: deduct full fare immediately
     * - Cash: record pending, driver confirms later
     * - No seat reservation needed
     */
    private BookingResponse handleIntraPull(BookingRequest req) {
        FareBreakdown fare = fareCalc.calculateIntraPull(
                req.getDistanceKm(),
                LocalDateTime.now().toLocalTime()
        );

        TaxiBooking booking = buildBaseBooking(req, fare);
        booking.setSeatsBooked(1);
        booking = bookingRepo.save(booking);

        Payment payment = createPayment(booking, fare.getTotalFare(),
                Payment.TYPE_FULL_PAYMENT, req.getPaymentMethod());

        String message = resolveIntraPullPayment(booking, payment, fare);

        return toResponse(booking, fare, message,
                fare.getTotalFare(), null, null);
    }

    /**
     * INTRA + RESERVED
     * - Calculate fare with reserved premium
     * - Charge deposit at booking; balance on trip completion
     * - Taxi guaranteed at scheduled time
     */
    private BookingResponse handleIntraReserved(BookingRequest req) {
        FareBreakdown fare = fareCalc.calculateIntraReserved(
                req.getDistanceKm(),
                req.getScheduledPickupTime().toLocalTime()
        );

        TaxiBooking booking = buildBaseBooking(req, fare);
        booking.setSeatsBooked(1);
        booking.setScheduledPickupTime(req.getScheduledPickupTime());
        booking.setBookingStatus(TaxiBookingStatus.PENDING);
        booking = bookingRepo.save(booking);

        // Charge deposit now; balance will be charged in completeTrip()
        Payment deposit = createPayment(booking, fare.getDepositAmount(),
                Payment.TYPE_DEPOSIT, req.getPaymentMethod());
        processPayment(deposit, req.getPaymentMethod());

        booking.setPaymentStatus(TaxiPaymentStatus.DEPOSIT_HELD);
        booking.setBookingStatus(TaxiBookingStatus.DEPOSIT_PAID);
        bookingRepo.save(booking);

        String msg = String.format(
                "Deposit of Nu %.0f charged. Balance of Nu %.0f due on trip completion.",
                fare.getDepositAmount(), fare.getBalanceAmount());

        return toResponse(booking, fare, msg,
                fare.getDepositAmount(), null, null);
    }

    /**
     * INTER + PULL  (shared seat on a driver-published route)
     * - Atomically decrement seats to prevent overbooking
     * - Full per-seat fare charged immediately (payment required to hold seat)
     */
    @Transactional
    private BookingResponse handleInterPull(BookingRequest req) {
        InterRoute route = getActiveRoute(req.getInterRouteId());

        int seats = req.getSeatsBooked() != null ? req.getSeatsBooked() : 1;

        // Atomic seat decrement — returns 0 if not enough seats
        int updated = routeRepo.decrementSeats(route.getId(), seats);
        if (updated == 0) {
            throw new IllegalStateException(
                    "Not enough seats available on route " + route.getId());
        }

        FareBreakdown fare = fareCalc.calculateInterPull(route, seats);

        TaxiBooking booking = buildBaseBooking(req, fare);
        booking.setInterRouteId(route.getId());
        booking.setSeatsBooked(seats);
        booking.setDistanceKm(route.getRouteDistanceKm());
        booking = bookingRepo.save(booking);

        // Full seat fare paid immediately — seat is only held after payment
        Payment payment = createPayment(booking, fare.getTotalFare(),
                Payment.TYPE_FULL_PAYMENT, req.getPaymentMethod());
        processPayment(payment, req.getPaymentMethod());

        booking.setPaymentStatus(TaxiPaymentStatus.FULLY_PAID);
        bookingRepo.save(booking);

        int remaining = route.getAvailableSeats() - seats;
        String msg = String.format(
                "%d seat(s) confirmed on route. Nu %.0f paid. %d seats remaining.",
                seats, fare.getTotalFare(), remaining);

        return toResponse(booking, fare, msg, fare.getTotalFare(),
                remaining, route.getId());
    }

    /**
     * INTER + RESERVED  (exclusive full-vehicle booking)
     * - All seats blocked for this passenger (no other bookings on this route)
     * - Deposit charged now; balance on departure/completion
     * - Driver guaranteed full vehicle revenue
     */
    @Transactional
    private BookingResponse handleInterReserved(BookingRequest req) {
        InterRoute route = getActiveRoute(req.getInterRouteId());

        // Reserve ALL seats
        int updated = routeRepo.decrementSeats(route.getId(), route.getAvailableSeats());
        if (updated == 0) {
            throw new IllegalStateException(
                    "Route " + route.getId() + " has no available seats to reserve.");
        }

        FareBreakdown fare = fareCalc.calculateInterReserved(route);

        TaxiBooking booking = buildBaseBooking(req, fare);
        booking.setInterRouteId(route.getId());
        booking.setSeatsBooked(route.getTotalSeats());
        booking.setScheduledPickupTime(route.getDepartureTime());
        booking.setDistanceKm(route.getRouteDistanceKm());
        booking = bookingRepo.save(booking);

        Payment deposit = createPayment(booking, fare.getDepositAmount(),
                Payment.TYPE_DEPOSIT, req.getPaymentMethod());
        processPayment(deposit, req.getPaymentMethod());

        booking.setPaymentStatus(TaxiPaymentStatus.DEPOSIT_HELD);
        booking.setBookingStatus(TaxiBookingStatus.DEPOSIT_PAID);
        bookingRepo.save(booking);

        String msg = String.format(
                "Full vehicle reserved. Deposit Nu %.0f charged. " +
                "Balance of Nu %.0f due on trip completion.",
                fare.getDepositAmount(), fare.getBalanceAmount());

        return toResponse(booking, fare, msg,
                fare.getDepositAmount(), 0, route.getId());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TRIP LIFECYCLE — called by separate TripController endpoints
    // ─────────────────────────────────────────────────────────────────────────

    /** Driver starts the trip */
    @Transactional
    public BookingResponse startTrip(Long bookingId, Long driverId) {
        TaxiBooking booking = getBooking(bookingId);
        booking.setDriverId(driverId);
        booking.setBookingStatus(TaxiBookingStatus.IN_PROGRESS);
        booking.setTripStartedAt(LocalDateTime.now());
        bookingRepo.save(booking);
        return toResponse(booking, null, "Trip started.", null, null, null);
    }

    /** Driver or system marks trip complete — collects balance for Reserved */
    @Transactional
    public BookingResponse completeTrip(Long bookingId) {
        TaxiBooking booking = getBooking(bookingId);
        booking.setBookingStatus(TaxiBookingStatus.COMPLETED);
        booking.setTripEndedAt(LocalDateTime.now());

        // Collect balance for Reserved mode
        if (booking.getTripMode() == TripMode.RESERVED
                && booking.getBalanceAmount() != null
                && booking.getBalanceAmount().compareTo(BigDecimal.ZERO) > 0) {

            Payment balance = createPayment(booking, booking.getBalanceAmount(),
                    Payment.TYPE_BALANCE, booking.getPaymentMethod());
            processPayment(balance, booking.getPaymentMethod());
            booking.setPaymentStatus(TaxiPaymentStatus.FULLY_PAID);
        }

        bookingRepo.save(booking);
        return toResponse(booking, null, "Trip completed. Payment settled.", null, null, null);
    }

    /** Cancel booking with refund logic */
    @Transactional
    public BookingResponse cancelBooking(Long bookingId, String reason, boolean byDriver) {
        TaxiBooking booking = getBooking(bookingId);

        booking.setCancelledAt(LocalDateTime.now());
        booking.setCancellationReason(reason);
        booking.setBookingStatus(byDriver
                ? TaxiBookingStatus.CANCELLED_BY_DRIVER
                : TaxiBookingStatus.CANCELLED_BY_PASSENGER);

        // Restore seats for inter-dzongkhag bookings
        if (booking.getInterRouteId() != null && booking.getSeatsBooked() != null) {
            routeRepo.incrementSeats(booking.getInterRouteId(), booking.getSeatsBooked());
        }

        // Refund logic
        handleCancellationRefund(booking);

        bookingRepo.save(booking);
        return toResponse(booking, null, "Booking cancelled. Refund processed.", null, null, null);
    }

    /** Driver confirms cash received (for CASH payment method) */
    @Transactional
    public void confirmCashReceived(Long bookingId) {
        TaxiBooking booking = getBooking(bookingId);
        paymentRepo.findByBookingIdOrderByCreatedAtAsc(bookingId)
                .stream()
                .filter(p -> p.getPaymentMethod() == TaxiPaymentMethod.CASH)
                .forEach(p -> {
                    p.setCashConfirmedByDriver(true);
                    p.setPaymentStatus(TaxiPaymentStatus.FULLY_PAID);
                    p.setSettledAt(LocalDateTime.now());
                    paymentRepo.save(p);
                });
        booking.setPaymentStatus(TaxiPaymentStatus.FULLY_PAID);
        bookingRepo.save(booking);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private TaxiBooking buildBaseBooking(BookingRequest req, FareBreakdown fare) {
        return TaxiBooking.builder()
                .passengerId(req.getPassengerId())
                .bookForOther(req.getBookForOther())
                .riderName(Boolean.TRUE.equals(req.getBookForOther())
                        ? req.getRiderName() : null)
                .riderPhone(Boolean.TRUE.equals(req.getBookForOther())
                        ? req.getRiderPhone() : null)
                .tripCategory(req.getTripCategory())
                .tripMode(req.getTripMode())
                .riderPickupLat(req.getRiderPickupLat())
                .riderPickupLng(req.getRiderPickupLng())
                .riderPickupAddress(req.getRiderPickupAddress())
                .dropOffLat(req.getDropOffLat())
                .dropOffLng(req.getDropOffLng())
                .dropOffAddress(req.getDropOffAddress())
                .distanceKm(req.getDistanceKm())
                .paymentMethod(req.getPaymentMethod())
                .paymentStatus(TaxiPaymentStatus.PENDING)
                .bookingStatus(TaxiBookingStatus.PENDING)
                // fare fields
                .baseFare(fare.getBaseFare())
                .distanceCharge(fare.getDistanceCharge())
                .nightSurcharge(fare.getNightSurcharge())
                .reservedPremium(fare.getReservedPremium())
                .surgeMultiplier(fare.getSurgeMultiplier())
                .totalFare(fare.getTotalFare())
                .depositAmount(fare.getDepositAmount())
                .balanceAmount(fare.getBalanceAmount())
                .commissionAmount(fare.getCommissionAmount())
                .driverNetAmount(fare.getDriverNetAmount())
                .build();
    }

    private Payment createPayment(TaxiBooking booking, BigDecimal amount,
                                   String type, TaxiPaymentMethod method) {
        Payment p = Payment.builder()
                .bookingId(booking.getId())
                .payerId(booking.getPassengerId())
                .paymentType(type)
                .amount(amount)
                .paymentMethod(method)
                .paymentStatus(TaxiPaymentStatus.PENDING)
                .build();
        return paymentRepo.save(p);
    }

    /**
     * Stub — in production connect to mPay/BOB/BNB gateway or deduct from Yaya Wallet.
     * For CASH: do nothing here; driver confirms later via confirmCashReceived().
     */
    private void processPayment(Payment payment, TaxiPaymentMethod method) {
        if (method == TaxiPaymentMethod.CASH) {
            log.info("Cash payment for booking {} — awaiting driver confirmation.",
                    payment.getBookingId());
            return;
        }
        // TODO: call gateway service (mPay, BOB, BNB, Yaya Wallet)
        payment.setPaymentStatus(TaxiPaymentStatus.FULLY_PAID);
        payment.setSettledAt(LocalDateTime.now());
        paymentRepo.save(payment);
        log.info("Payment {} of Nu {} processed via {}.",
                payment.getId(), payment.getAmount(), method);
    }

    private String resolveIntraPullPayment(TaxiBooking booking, Payment payment, FareBreakdown fare) {
        if (booking.getPaymentMethod() == TaxiPaymentMethod.CASH) {
            return String.format("Booking confirmed. Pay Nu %.0f cash to driver.", fare.getTotalFare());
        }
        processPayment(payment, booking.getPaymentMethod());
        booking.setPaymentStatus(TaxiPaymentStatus.FULLY_PAID);
        bookingRepo.save(booking);
        return String.format("Nu %.0f paid via %s. Driver on the way.",
                fare.getTotalFare(), booking.getPaymentMethod());
    }

    private void handleCancellationRefund(TaxiBooking booking) {
        // Reserved: if cancelled before driver departs → 100% refund of deposit
        // Pull: if cancelled before driver accepted → 100% refund
        // Otherwise → partial or no refund (implement business rules here)
        if (booking.getPaymentStatus() == TaxiPaymentStatus.DEPOSIT_HELD
                || booking.getPaymentStatus() == TaxiPaymentStatus.FULLY_PAID) {

            BigDecimal refundAmount = booking.getTripMode() == TripMode.RESERVED
                    ? booking.getDepositAmount()   // deposit only; balance not yet charged
                    : booking.getTotalFare();       // full refund for pull if not started

            if (booking.getTripStartedAt() != null) {
                // Trip already started — no refund
                refundAmount = BigDecimal.ZERO;
            }

            if (refundAmount != null && refundAmount.compareTo(BigDecimal.ZERO) > 0) {
                Payment refund = createPayment(booking, refundAmount,
                        Payment.TYPE_REFUND, booking.getPaymentMethod());
                refund.setPaymentStatus(TaxiPaymentStatus.FULLY_PAID);
                refund.setSettledAt(LocalDateTime.now());
                paymentRepo.save(refund);
                booking.setPaymentStatus(TaxiPaymentStatus.FULLY_REFUNDED);
            }
        } else {
            booking.setBookingStatus(TaxiBookingStatus.REFUNDED);
        }
    }

    private InterRoute getActiveRoute(Long routeId) {
        return routeRepo.findByIdAndIsActiveTrue(routeId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Active route not found: " + routeId));
    }

    private TaxiBooking getBooking(Long bookingId) {
        return bookingRepo.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Booking not found: " + bookingId));
    }

    private void validate(BookingRequest req) {
        if (Boolean.TRUE.equals(req.getBookForOther())) {
            if (req.getRiderPhone() == null || req.getRiderPhone().isBlank())
                throw new IllegalArgumentException("Rider phone required when booking for another person.");
        }
        if (req.getTripCategory() == TripCategory.INTER_DZONGKHAG
                && req.getInterRouteId() == null) {
            throw new IllegalArgumentException("interRouteId required for inter-dzongkhag booking.");
        }
        if (req.getTripMode() == TripMode.RESERVED
                && req.getTripCategory() == TripCategory.INTRA_DZONGKHAG
                && req.getScheduledPickupTime() == null) {
            throw new IllegalArgumentException("scheduledPickupTime required for Intra Reserved.");
        }
    }

    private BookingResponse toResponse(TaxiBooking b, FareBreakdown fare,
                                        String message, BigDecimal amountDueNow,
                                        Integer seatsRemaining, Long routeId) {
        return BookingResponse.builder()
                .bookingId(b.getId())
                .tripCategory(b.getTripCategory())
                .tripMode(b.getTripMode())
                .bookingStatus(b.getBookingStatus())
                .paymentStatus(b.getPaymentStatus())
                .paymentMethod(b.getPaymentMethod())
                .riderName(b.getRiderName())
                .riderPhone(b.getRiderPhone())
                .bookForOther(b.getBookForOther())
                .riderPickupAddress(b.getRiderPickupAddress())
                .dropOffAddress(b.getDropOffAddress())
                .interRouteId(routeId)
                .seatsBooked(b.getSeatsBooked())
                .availableSeatsAfterBooking(seatsRemaining)
                .scheduledPickupTime(b.getScheduledPickupTime())
                .fareBreakdown(fare)
                .amountDueNow(amountDueNow)
                .paymentMessage(message)
                .createdAt(b.getCreatedAt())
                .build();
    }
}
