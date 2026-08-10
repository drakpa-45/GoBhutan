package com.goBhutan.adminPanel.taxi.service;

import com.goBhutan.adminPanel.paymentInt.dto.ServicePaymentRequest;
import com.goBhutan.adminPanel.paymentInt.dto.WalletPaymentResult;
import com.goBhutan.adminPanel.paymentInt.service.PaymentIntegrationService;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaxiBookingService {

    private final TaxiBookingRepository bookingRepo;
    private final InterRouteRepository  routeRepo;
    private final PaymentRepository     paymentRepo;
    private final FareCalculatorService fareCalc;

    @Autowired
    private PaymentIntegrationService paymentService;
    // ─────────────────────────────────────────────────────────────────────────
    // CREATE BOOKING
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public BookingResponse createBooking(BookingRequest req) {
        validate(req);

        TripCategory cat  = req.getTripCategory();
        TripMode     mode = req.getTripMode();

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
    // All modes: app charges baseFare only at booking time.
    // Remaining balance (totalFare - baseFare) is paid directly to driver
    // via cash or their own mPay/bank scan — no app involvement.
    // ─────────────────────────────────────────────────────────────────────────

    private BookingResponse handleIntraPull(BookingRequest req) {
        FareBreakdown fare = fareCalc.calculateIntraPull(
                req.getDistanceKm(),
                LocalDateTime.now().toLocalTime()
        );

        TaxiBooking booking = buildBaseBooking(req, fare);
        booking.setSeatsBooked(1);
        booking = bookingRepo.save(booking);

        // Charge baseFare only
        Payment payment = createPayment(booking, fare.getBaseFare(),
                Payment.TYPE_DEPOSIT, req.getPaymentMethod());
        processPayment(payment, req.getPaymentMethod());

        booking.setPaymentStatus(TaxiPaymentStatus.DEPOSIT_HELD);
        bookingRepo.save(booking);

        String msg = String.format(
                "Base fare Nu %.0f charged. Pay remaining Nu %.0f directly to driver.",
                fare.getBaseFare(), fare.getBalanceAmount());

        return toResponse(booking, fare, msg, fare.getBaseFare(), null, null);
    }

    private BookingResponse handleIntraReserved(BookingRequest req) {
        FareBreakdown fare = fareCalc.calculateIntraReserved(
                req.getDistanceKm(),
                req.getScheduledPickupTime().toLocalTime()
        );

        TaxiBooking booking = buildBaseBooking(req, fare);
        booking.setSeatsBooked(1);
        booking.setScheduledPickupTime(req.getScheduledPickupTime());
        booking = bookingRepo.save(booking);

        // Charge baseFare only
        Payment payment = createPayment(booking, fare.getBaseFare(),
                Payment.TYPE_DEPOSIT, req.getPaymentMethod());
        processPayment(payment, req.getPaymentMethod());

        booking.setPaymentStatus(TaxiPaymentStatus.DEPOSIT_HELD);
        booking.setBookingStatus(TaxiBookingStatus.DEPOSIT_PAID);
        bookingRepo.save(booking);

        String msg = String.format(
                "Base fare Nu %.0f charged. Pay remaining Nu %.0f directly to driver on arrival.",
                fare.getBaseFare(), fare.getBalanceAmount());

        return toResponse(booking, fare, msg, fare.getBaseFare(), null, null);
    }

    @Transactional
    public BookingResponse handleInterPull(BookingRequest req) {
        InterRoute route = getActiveRoute(req.getInterRouteId());

        int seats = req.getSeatsBooked() != null ? req.getSeatsBooked() : 1;

        int updated = routeRepo.decrementSeats(route.getId(), seats);
        if (updated == 0)
            throw new IllegalStateException(
                    "Not enough seats available on route " + route.getId());

        // After
        FareBreakdown fare = fareCalc.calculateInterPull(
                route, seats,
                req.getBoardingStopId(),
                req.getAlightingStopId());

        TaxiBooking booking = buildBaseBooking(req, fare);
        booking.setInterRouteId(route.getId());
        booking.setSeatsBooked(seats);
        booking.setDistanceKm(route.getRouteDistanceKm());
        booking = bookingRepo.save(booking);

        // Charge baseFare only — seat held after payment
        Payment payment = createPayment(booking, fare.getBaseFare(),
                Payment.TYPE_DEPOSIT, req.getPaymentMethod());
        processPayment(payment, req.getPaymentMethod());

        booking.setPaymentStatus(TaxiPaymentStatus.DEPOSIT_HELD);
        bookingRepo.save(booking);

        int remaining = route.getAvailableSeats() - seats;
        String msg = String.format(
                "%d seat(s) confirmed. Base fare Nu %.0f charged. " +
                        "Pay remaining Nu %.0f directly to driver. %d seats left.",
                seats, fare.getBaseFare(), fare.getBalanceAmount(), remaining);

        return toResponse(booking, fare, msg, fare.getBaseFare(), remaining, route.getId());
    }

    @Transactional
    public BookingResponse handleInterReserved(BookingRequest req) {
        InterRoute route = getActiveRoute(req.getInterRouteId());

        int updated = routeRepo.decrementSeats(route.getId(), route.getAvailableSeats());
        if (updated == 0)
            throw new IllegalStateException(
                    "Route " + route.getId() + " has no available seats to reserve.");

        FareBreakdown fare = fareCalc.calculateInterReserved(
                route,
                req.getBoardingStopId(),
                req.getAlightingStopId());

        TaxiBooking booking = buildBaseBooking(req, fare);
        booking.setInterRouteId(route.getId());
        booking.setSeatsBooked(route.getTotalSeats());
        booking.setScheduledPickupTime(route.getDepartureTime());
        booking.setDistanceKm(route.getRouteDistanceKm());
        booking = bookingRepo.save(booking);

        // Charge baseFare only
        Payment payment = createPayment(booking, fare.getBaseFare(),
                Payment.TYPE_DEPOSIT, req.getPaymentMethod());
        processPayment(payment, req.getPaymentMethod());

        booking.setPaymentStatus(TaxiPaymentStatus.DEPOSIT_HELD);
        booking.setBookingStatus(TaxiBookingStatus.DEPOSIT_PAID);
        bookingRepo.save(booking);

        String msg = String.format(
                "Full vehicle reserved. Base fare Nu %.0f charged. " +
                        "Pay remaining Nu %.0f directly to driver on departure.",
                fare.getBaseFare(), fare.getBalanceAmount());

        return toResponse(booking, fare, msg, fare.getBaseFare(), 0, route.getId());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TRIP LIFECYCLE
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public BookingResponse startTrip(Long bookingId, String driverId) {
        TaxiBooking booking = getBooking(bookingId);
        booking.setDriverId(driverId);
        booking.setBookingStatus(TaxiBookingStatus.IN_PROGRESS);
        booking.setTripStartedAt(LocalDateTime.now());
        bookingRepo.save(booking);
        return toResponse(booking, null, "Trip started.", null, null, null);
    }

    /**
     * Trip complete — no balance collection.
     * Passenger already paid remaining directly to driver.
     * Just mark completed and settle platform commission on baseFare.
     */
    @Transactional
    public BookingResponse completeTrip(Long bookingId) {
        TaxiBooking booking = getBooking(bookingId);
        booking.setBookingStatus(TaxiBookingStatus.COMPLETED);
        booking.setTripEndedAt(LocalDateTime.now());
        booking.setPaymentStatus(TaxiPaymentStatus.FULLY_PAID);
        bookingRepo.save(booking);

        log.info("Trip {} completed. Commission Nu {} on base fare. " +
                        "Driver net from app: Nu {}. Remaining Nu {} settled directly.",
                bookingId,
                booking.getCommissionAmount(),
                booking.getDriverNetAmount(),
                booking.getBalanceAmount());

        return toResponse(booking, null, "Trip completed.", null, null, null);
    }

    @Transactional
    public BookingResponse cancelBooking(Long bookingId, String reason, boolean byDriver) {
        TaxiBooking booking = getBooking(bookingId);

        booking.setCancelledAt(LocalDateTime.now());
        booking.setCancellationReason(reason);
        booking.setBookingStatus(byDriver
                ? TaxiBookingStatus.CANCELLED_BY_DRIVER
                : TaxiBookingStatus.CANCELLED_BY_PASSENGER);

        // Restore seats for inter-dzongkhag
        if (booking.getInterRouteId() != null && booking.getSeatsBooked() != null)
            routeRepo.incrementSeats(booking.getInterRouteId(), booking.getSeatsBooked());

        handleCancellationRefund(booking);
        bookingRepo.save(booking);

        return toResponse(booking, null, "Booking cancelled. Refund processed.", null, null, null);
    }

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
        // balanceAmount = totalFare - baseFare (passenger pays this directly to driver)
        BigDecimal balanceAmount = fare.getTotalFare().subtract(fare.getBaseFare());

        return TaxiBooking.builder()
                .passengerId(req.getPassengerId())
                .bookForOther(req.getBookForOther())
                .riderName(Boolean.TRUE.equals(req.getBookForOther()) ? req.getRiderName() : null)
                .riderPhone(Boolean.TRUE.equals(req.getBookForOther()) ? req.getRiderPhone() : null)
                .tripCategory(req.getTripCategory())
                .tripMode(req.getTripMode())
                .riderPickupLat(req.getRiderPickupLat())
                .riderPickupLng(req.getRiderPickupLng())
                .riderPickupAddress(req.getRiderPickupAddress())
                .dropOffLat(req.getDropOffLat())
                .dropOffLng(req.getDropOffLng())
                .dropOffAddress(req.getDropOffAddress())
                .distanceKm(req.getDistanceKm())
                .boardingStopId(req.getBoardingStopId())
                .alightingStopId(req.getAlightingStopId())
                .paymentMethod(req.getPaymentMethod())
                .paymentStatus(TaxiPaymentStatus.PENDING)
                .bookingStatus(TaxiBookingStatus.PENDING)
                .baseFare(fare.getBaseFare())
                .distanceCharge(fare.getDistanceCharge())
                .nightSurcharge(fare.getNightSurcharge())
                .reservedPremium(fare.getReservedPremium())
                .surgeMultiplier(fare.getSurgeMultiplier())
                .totalFare(fare.getTotalFare())
                .depositAmount(fare.getBaseFare())       // app charges baseFare only
                .balanceAmount(balanceAmount)            // paid directly to driver
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

    private void processPayment(Payment payment, TaxiPaymentMethod method) {
        if (method == TaxiPaymentMethod.CASH) {
            log.info("Cash payment for booking {} — awaiting driver confirmation.",
                    payment.getBookingId());
            return;
        }

        if (method == TaxiPaymentMethod.YAYA_WALLET) {
            Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String userId = jwt.getSubject();

            if (payment.getGatewayReference() != null && !payment.getGatewayReference().isBlank())
                throw new RuntimeException("Wallet payment already processed for this booking.");

            ServicePaymentRequest paymentRequest = new ServicePaymentRequest();
            paymentRequest.setAmount(payment.getAmount());
            paymentRequest.setCurrency("BTN");
            paymentRequest.setServiceName("TAXI");
            paymentRequest.setReferenceType("TAXI_BOOKING");
            paymentRequest.setReferenceId(String.valueOf(payment.getBookingId()));
            paymentRequest.setDescription("Taxi booking base fare payment");

            WalletPaymentResult walletPayment = paymentService.payWithWallet(paymentRequest, userId);

            paymentService.creditServiceSettlement(
                    walletPayment.getPaymentRef(),
                    payment.getAmount(),
                    "TAXI",
                    "TAXI_BOOKING",
                    String.valueOf(payment.getBookingId()),
                    "Taxi booking settlement",
                    userId
            );

            payment.setGatewayReference(walletPayment.getPaymentRef());
            log.info("Wallet payment {} of Nu {} processed. Ref: {}",
                    payment.getId(), payment.getAmount(), walletPayment.getPaymentRef());
        }

        // mPay / BOB / BNB — TODO: integrate gateway
        payment.setPaymentStatus(TaxiPaymentStatus.FULLY_PAID);
        payment.setSettledAt(LocalDateTime.now());
        paymentRepo.save(payment);
        log.info("Base fare payment {} of Nu {} processed via {}.",
                payment.getId(), payment.getAmount(), method);
    }

    private void handleCancellationRefund(TaxiBooking booking) {
        // Refund only the baseFare that was charged by the app
        // Remaining balance was never captured — nothing to refund
        if (booking.getPaymentStatus() == TaxiPaymentStatus.DEPOSIT_HELD
                || booking.getPaymentStatus() == TaxiPaymentStatus.FULLY_PAID) {

            BigDecimal refundAmount = booking.getTripStartedAt() != null
                    ? BigDecimal.ZERO              // trip already started — no refund
                    : booking.getDepositAmount();  // refund baseFare charged by app

            if (refundAmount.compareTo(BigDecimal.ZERO) > 0) {
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
        if (Boolean.TRUE.equals(req.getBookForOther()))
            if (req.getRiderPhone() == null || req.getRiderPhone().isBlank())
                throw new IllegalArgumentException("Rider phone required when booking for another person.");

        if (req.getTripCategory() == TripCategory.INTER_DZONGKHAG && req.getInterRouteId() == null)
            throw new IllegalArgumentException("interRouteId required for inter-dzongkhag booking.");

        if (req.getTripMode() == TripMode.RESERVED
                && req.getTripCategory() == TripCategory.INTRA_DZONGKHAG
                && req.getScheduledPickupTime() == null)
            throw new IllegalArgumentException("scheduledPickupTime required for Intra Reserved.");
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

    /**
     * Driver declines the booking.
     * Refunds baseFare back to passenger and frees seats.
     */
    @Transactional
    public BookingResponse declineBooking(Long bookingId, String driverId) {
        TaxiBooking booking = getBooking(bookingId);

        if (booking.getBookingStatus() != TaxiBookingStatus.PENDING)
            throw new IllegalStateException("Only PENDING bookings can be declined.");

        booking.setBookingStatus(TaxiBookingStatus.DRIVER_DECLINED);
        booking.setCancelledAt(LocalDateTime.now());
        booking.setCancellationReason("Driver declined the request.");

        // Restore seats for inter-dzongkhag
        if (booking.getInterRouteId() != null && booking.getSeatsBooked() != null)
            routeRepo.incrementSeats(booking.getInterRouteId(), booking.getSeatsBooked());

        // Refund baseFare back to passenger wallet
        if (booking.getDepositAmount() != null
                && booking.getDepositAmount().compareTo(BigDecimal.ZERO) > 0
                && booking.getPaymentMethod() == TaxiPaymentMethod.YAYA_WALLET) {

            // Find the original payment to get the gateway reference
            Payment original = paymentRepo.findByBookingIdOrderByCreatedAtAsc(bookingId)
                    .stream()
                    .filter(p -> p.getPaymentType().equals(Payment.TYPE_DEPOSIT))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Original payment not found for refund."));

            // Refund back to the passenger's wallet using original gateway reference
            paymentService.refundToWallet(
                    original.getGatewayReference(),   // original transaction ref
                    booking.getDepositAmount(),
                    "TAXI",
                    "TAXI_BOOKING",
                    String.valueOf(bookingId),
                    "Refund — driver declined booking",
                    original.getPayerId()
            );

            Payment refund = createPayment(booking, booking.getDepositAmount(),
                    Payment.TYPE_REFUND, TaxiPaymentMethod.YAYA_WALLET);
            refund.setGatewayReference(original.getGatewayReference());
            refund.setPaymentStatus(TaxiPaymentStatus.FULLY_PAID);
            refund.setSettledAt(LocalDateTime.now());
            paymentRepo.save(refund);

            booking.setPaymentStatus(TaxiPaymentStatus.FULLY_REFUNDED);
            log.info("Wallet refund of Nu {} processed for booking {} — driver declined.",
                    booking.getDepositAmount(), bookingId);
        }

        bookingRepo.save(booking);
        return toResponse(booking, null,
                "Driver declined. Base fare refunded to your account.", null, null, null);
    }
}