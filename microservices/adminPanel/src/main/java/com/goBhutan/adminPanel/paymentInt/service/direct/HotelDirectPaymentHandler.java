package com.goBhutan.adminPanel.paymentInt.service.direct;

import com.goBhutan.adminPanel.hotel.entity.Booking;
import com.goBhutan.adminPanel.hotel.service.BookingService;
import com.goBhutan.adminPanel.paymentInt.dto.DirectPaymentInitiateRequest;
import com.goBhutan.adminPanel.paymentInt.dto.ServicePaymentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HotelDirectPaymentHandler implements DirectPaymentHandler {

    private final BookingService hotelBookingService;

    @Override
    public String module() { return "hotel"; }

    @Override
    public String referenceType() { return "HOTEL_BOOKING"; }

    @Override
    public String referenceIdKey() { return "bookingRef"; }

    @Override
    public String confirmationKey() { return "booking"; }

    @Override
    public ServicePaymentRequest buildPaymentRequest(String referenceId, String userId,
                                                     DirectPaymentInitiateRequest request) {
        return hotelBookingService.buildDirectGatewayPaymentRequest(
                referenceId, userId,
                request.getAmount(), request.getCurrency(), request.getDescription());
    }

    @Override
    public void onPaymentPending(String referenceId, String userId, LocalDateTime expiresAt) {
        hotelBookingService.extendDirectGatewayPaymentLock(referenceId, userId, expiresAt);
    }

    @Override
    public String ensurePaymentCanContinue(String paymentRef, String userId) {
        return hotelBookingService.ensureDirectGatewayPaymentCanContinue(paymentRef, userId);
    }

    @Override
    public String ensurePaymentCanDebit(String paymentRef, String userId) {
        return hotelBookingService.ensureDirectGatewayPaymentCanDebit(paymentRef, userId);
    }

    @Override
    public Object confirmPaymentSuccess(String paymentRef, String userId) {
        Booking booking = hotelBookingService.confirmDirectGatewayPaymentBooking(paymentRef, userId);
        return toConfirmationResponse(booking);
    }

    private Map<String, Object> toConfirmationResponse(Booking booking) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("bookingRef", booking.getBookingReference());
        response.put("paymentMethod", booking.getPaymentMethod());
        response.put("paymentRef", booking.getWalletPaymentRef());
        response.put("hotelId", booking.getHotel().getId());
        response.put("roomId", booking.getRoom().getId());
        response.put("checkIn", booking.getCheckInDate());
        response.put("checkOut", booking.getCheckOutDate());
        response.put("status", booking.getStatus());
        return response;
    }
}