package com.goBhutan.adminPanel.busAdmin.service;

import com.goBhutan.adminPanel.busAdmin.entity.SeatBooking;
import com.goBhutan.adminPanel.paymentInt.dto.DirectPaymentInitiateRequest;
import com.goBhutan.adminPanel.paymentInt.dto.ServicePaymentRequest;
import com.goBhutan.adminPanel.paymentInt.service.direct.DirectPaymentHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BusDirectPaymentHandler implements DirectPaymentHandler {

    private static final String BUS_BOOKING_REFERENCE_TYPE = "BUS_BOOKING";

    private final BusBookingService bookingService;

    @Override
    public String module() {
        return "bus";
    }

    @Override
    public String referenceType() {
        return BUS_BOOKING_REFERENCE_TYPE;
    }

    @Override
    public String referenceIdKey() {
        return "bookingRef";
    }

    @Override
    public String confirmationKey() {
        return "booking";
    }

    @Override
    public ServicePaymentRequest buildPaymentRequest(
            String referenceId,
            String userId,
            DirectPaymentInitiateRequest request
    ) {
        return bookingService.buildDirectGatewayPaymentRequest(
                referenceId,
                userId,
                request.getAmount(),
                request.getCurrency(),
                request.getDescription());
    }

    @Override
    public void onPaymentPending(String referenceId, String userId, LocalDateTime expiresAt) {
        bookingService.extendDirectGatewayPaymentLock(referenceId, userId, expiresAt);
    }

    @Override
    public String ensurePaymentCanContinue(String paymentRef, String userId) {
        return bookingService.ensureDirectGatewayPaymentCanContinue(paymentRef, userId);
    }

    @Override
    public String ensurePaymentCanDebit(String paymentRef, String userId) {
        return bookingService.ensureDirectGatewayPaymentCanDebit(paymentRef, userId);
    }

    @Override
    public Object confirmPaymentSuccess(String paymentRef, String userId) {
        List<SeatBooking> bookings = bookingService.confirmDirectGatewayPaymentBooking(paymentRef, userId);
        return toBookingPaymentResponse(bookings.get(0).getBookingRef(), bookings);
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
