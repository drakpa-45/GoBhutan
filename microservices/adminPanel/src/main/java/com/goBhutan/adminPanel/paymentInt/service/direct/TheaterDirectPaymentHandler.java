package com.goBhutan.adminPanel.paymentInt.service.direct;

import com.goBhutan.adminPanel.theater.entity.Ticket;
import com.goBhutan.adminPanel.theater.entity.TheaterBooking;
import com.goBhutan.adminPanel.theater.service.TheaterBookingService;
import com.goBhutan.adminPanel.paymentInt.dto.DirectPaymentInitiateRequest;
import com.goBhutan.adminPanel.paymentInt.dto.ServicePaymentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TheaterDirectPaymentHandler implements DirectPaymentHandler {

    private final TheaterBookingService theaterBookingService;

    @Override
    public String module() { return "theater"; }

    @Override
    public String referenceType() { return "THEATER_BOOKING"; }

    @Override
    public String referenceIdKey() { return "bookingRef"; }

    @Override
    public String confirmationKey() { return "booking"; }

    @Override
    public ServicePaymentRequest buildPaymentRequest(String referenceId, String userId,
                                                     DirectPaymentInitiateRequest request) {
        return theaterBookingService.buildDirectGatewayPaymentRequest(
                referenceId, userId,
                request.getAmount(), request.getCurrency(), request.getDescription());
    }

    @Override
    public void onPaymentPending(String referenceId, String userId, LocalDateTime expiresAt) {
        theaterBookingService.extendDirectGatewayPaymentLock(referenceId, userId, expiresAt);
    }

    @Override
    public String ensurePaymentCanContinue(String paymentRef, String userId) {
        return theaterBookingService.ensureDirectGatewayPaymentCanContinue(paymentRef, userId);
    }

    @Override
    public String ensurePaymentCanDebit(String paymentRef, String userId) {
        return theaterBookingService.ensureDirectGatewayPaymentCanDebit(paymentRef, userId);
    }

    @Override
    public Object confirmPaymentSuccess(String paymentRef, String userId) {
        List<Ticket> tickets = theaterBookingService.confirmDirectGatewayPaymentBooking(paymentRef, userId);
        return toConfirmationResponse(tickets);
    }

    private Map<String, Object> toConfirmationResponse(List<Ticket> tickets) {
        Ticket first = tickets.get(0);
        TheaterBooking booking = first.getBooking();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("bookingRef", booking.getBookingRef());
        response.put("paymentMethod", booking.getPaymentMethod());
        response.put("paymentRef", booking.getWalletPaymentRef());
        response.put("screeningId", first.getScreening().getId());
        response.put("totalTickets", tickets.size());
        response.put("tickets", tickets.stream().map(t -> Map.of(
                "ticketNumber", t.getTicketNumber(),
                "seatId", t.getSeat().getId(),
                "seatIdentifier", t.getSeat().getSeatIdentifier(),
                "customerName", t.getCustomerName(),
                "status", booking.getBookingStatus().getStatusName()
        )).toList());
        return response;
    }
}