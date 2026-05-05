package com.goBhutan.adminPanel.busAdmin.controller;

import com.goBhutan.adminPanel.busAdmin.dto.BusTicketResponse;
import com.goBhutan.adminPanel.busAdmin.dto.ConfirmBookingRequest;
import com.goBhutan.adminPanel.busAdmin.dto.DirectGatewayPaymentAccountInquiryRequest;
import com.goBhutan.adminPanel.busAdmin.dto.DirectGatewayPaymentDebitRequest;
import com.goBhutan.adminPanel.busAdmin.dto.DirectGatewayPaymentInitiateRequest;
import com.goBhutan.adminPanel.busAdmin.dto.LockSeatRequest;
import com.goBhutan.adminPanel.busAdmin.entity.SeatBooking;
import com.goBhutan.adminPanel.busAdmin.service.BusBookingService;
import com.goBhutan.adminPanel.busAdmin.service.TicketService;
import com.goBhutan.adminPanel.common.dto.ApiResponse;
import com.goBhutan.adminPanel.paymentInt.dto.GatewayPaymentAccountInquiryResponse;
import com.goBhutan.adminPanel.paymentInt.dto.GatewayPaymentDebitResponse;
import com.goBhutan.adminPanel.paymentInt.dto.GatewayPaymentInitiateResponse;
import com.goBhutan.adminPanel.paymentInt.dto.PaymentStatusResponse;
import com.goBhutan.adminPanel.paymentInt.dto.ServicePaymentRequest;
import com.goBhutan.adminPanel.paymentInt.enums.PaymentStatus;
import com.goBhutan.adminPanel.paymentInt.service.PaymentIntegrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BusBookingController {

    private static final String PAYMENT_METHOD_DIRECT_GATEWAY = "DIRECT_GATEWAY";
    private static final String DIRECT_GATEWAY_PAYMENT_DESCRIPTION = "Direct Gateway Payment";

    private final BusBookingService bookingService;
    private final TicketService ticketService;
    private final PaymentIntegrationService paymentService;

    @PostMapping("/lock")
    public ResponseEntity<?> lock(@RequestBody LockSeatRequest req) {
        String userId = currentUserId();

        List<SeatBooking> bookings = bookingService.lockSeats(
                req.getScheduleId(),
                req.getSeatNumbers(),
                userId,
                req.getApplicantCid(),
                req.getApplicantMobile(),
                req.getApplicantEmail());

        return ResponseEntity.ok(Map.of(
                "bookingRef", bookings.get(0).getBookingRef(),
                "seatLabel", bookings.get(0).getSeatLabel(),
                "expiresAt", bookings.get(0).getLockExpiry(),
                "totalAmount", bookings.stream()
                        .map(SeatBooking::getFinalFareAtBooking)
                        .reduce(BigDecimal.ZERO, BigDecimal::add),
                "seats", bookings));
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> confirm(@RequestBody ConfirmBookingRequest req) {
        return confirmWithPaymentMethod(req);
    }

    @PostMapping("/pay")
    public ResponseEntity<?> pay(@RequestBody ConfirmBookingRequest req) {
        return confirmWithPaymentMethod(req);
    }

    @PostMapping("/direct-gateway-payment/initiate")
    public ResponseEntity<?> initiateDirectGatewayPayment(@RequestBody DirectGatewayPaymentInitiateRequest req) {
        String userId = currentUserId();

        ServicePaymentRequest paymentRequest = bookingService.buildDirectGatewayPaymentRequest(
                req.getBookingRef(),
                userId,
                req.getAmount(),
                req.getCurrency(),
                req.getDescription());
        GatewayPaymentInitiateResponse payment = paymentService.initiateGatewayServicePayment(
                paymentRequest,
                userId,
                req.getRemitterEmail());

        if (payment.getStatus() == PaymentStatus.PENDING) {
            bookingService.extendDirectGatewayPaymentLock(req.getBookingRef(), userId, payment.getExpiresAt());
        }

        return directGatewayPaymentResponse(payment.getStatus(), directGatewayPaymentInitiateResponse(
                req.getBookingRef(),
                payment));
    }

    @PostMapping("/direct-gateway-payment/account-inquiry")
    public ResponseEntity<?> directGatewayPaymentAccountInquiry(@RequestBody DirectGatewayPaymentAccountInquiryRequest req) {
        String userId = currentUserId();
        String bookingRef = bookingService.ensureDirectGatewayPaymentCanContinue(req.getPaymentRef(), userId);

        GatewayPaymentAccountInquiryResponse payment = paymentService.verifyGatewayServicePaymentAccount(
                req.getPaymentRef(),
                req.getRemitterBankId(),
                req.getRemitterAccNo(),
                userId);

        return directGatewayPaymentResponse(payment.getStatus(), Map.of(
                "bookingRef", bookingRef,
                "paymentMethod", PAYMENT_METHOD_DIRECT_GATEWAY,
                "payment", payment));
    }

    @PostMapping("/direct-gateway-payment/debit")
    public ResponseEntity<?> directGatewayPaymentDebit(@RequestBody DirectGatewayPaymentDebitRequest req) {
        String userId = currentUserId();
        String bookingRef = bookingService.ensureDirectGatewayPaymentCanDebit(req.getPaymentRef(), userId);

        GatewayPaymentDebitResponse payment = paymentService.submitGatewayServicePaymentOtp(
                req.getPaymentRef(),
                req.getOtp(),
                userId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("bookingRef", bookingRef);
        response.put("paymentMethod", PAYMENT_METHOD_DIRECT_GATEWAY);
        response.put("payment", payment);
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            List<SeatBooking> bookings = bookingService.confirmDirectGatewayPaymentBooking(req.getPaymentRef(), userId);
            response.put("booking", toBookingPaymentResponse(bookings.get(0).getBookingRef(), bookings));
        }

        return directGatewayPaymentResponse(payment.getStatus(), response);
    }

    @GetMapping("/direct-gateway-payment/status/{paymentRef}")
    public ResponseEntity<?> directGatewayPaymentStatus(@PathVariable String paymentRef) {
        String userId = currentUserId();

        PaymentStatusResponse payment = paymentService.getGatewayServicePaymentStatus(paymentRef, userId);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("paymentMethod", PAYMENT_METHOD_DIRECT_GATEWAY);
        response.put("payment", payment);
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            List<SeatBooking> bookings = bookingService.confirmDirectGatewayPaymentBooking(paymentRef, userId);
            response.put("bookingRef", bookings.get(0).getBookingRef());
            response.put("booking", toBookingPaymentResponse(bookings.get(0).getBookingRef(), bookings));
        }

        return directGatewayPaymentResponse(payment.getStatus(), response);
    }

    @PostMapping("/admin/cash/lock")
    public ResponseEntity<?> lockCashBooking(@RequestBody LockSeatRequest req) {
        String adminUserId = currentUserId();

        List<SeatBooking> bookings = bookingService.lockCashSeats(
                req.getScheduleId(),
                req.getSeatNumbers(),
                adminUserId,
                req.getApplicantCid(),
                req.getApplicantMobile(),
                req.getApplicantEmail());

        return ResponseEntity.ok(Map.of(
                "bookingRef", bookings.get(0).getBookingRef(),
                "seatLabel", bookings.get(0).getSeatLabel(),
                "expiresAt", bookings.get(0).getLockExpiry(),
                "paymentMode", "CASH",
                "seats", bookings));
    }



    @PostMapping("/admin/cash/confirm")
    public ResponseEntity<?> confirmCashBooking(@RequestBody ConfirmBookingRequest req) {
        String adminUserId = currentUserId();

        List<SeatBooking> bookings = bookingService.confirmCashBooking(req.getBookingRef(), adminUserId);

        return ResponseEntity.ok(Map.of(
                "message", "Booking confirmed",
                "bookingRef", req.getBookingRef(),
                "paymentMode", "CASH",
                "scheduleId", bookings.get(0).getSchedule().getId(),
                "totalSeats", bookings.size(),
                "seats", bookings.stream().map(b -> Map.of(
                        "bookingId", b.getId(),
                        "seatNumber", b.getSeatNumber(),
                        "seatLabel", b.getSeatLabel(),
                        "status", b.getStatus())).toList()));
    }

    @PostMapping("/cancel/{bookingId}")
    public ResponseEntity<?> cancel(@PathVariable Long bookingId) {
        String userId = currentUserId();

        SeatBooking booking = bookingService.cancel(bookingId, userId);

        return ResponseEntity.ok(Map.of(
                "bookingId", booking.getId(),
                "status", "CANCELLED"));
    }

    @PostMapping("/admin/cash/cancel/{bookingId}")
    public ResponseEntity<?> cancelCashBooking(@PathVariable Long bookingId) {
        String adminUserId = currentUserId();

        SeatBooking booking = bookingService.cancelCashByAdmin(bookingId, adminUserId);

        return ResponseEntity.ok(Map.of(
                "bookingId", booking.getId(),
                "paymentMode", "CASH",
                "status", "CANCELLED"));
    }

    @GetMapping("/schedule/{scheduleId}/seats")
    public ResponseEntity<?> getSeatStatus(@PathVariable Long scheduleId) {
        return ResponseEntity.ok(bookingService.getSeatStatus(scheduleId));
    }

    @GetMapping("/ticket/{bookingId}")
    public ResponseEntity<ApiResponse<BusTicketResponse>> getTicket(@PathVariable Long bookingId) {
        return ResponseEntity.ok(ApiResponse.success(ticketService.getTicketDetails(bookingId, currentUserId())));
    }

    @GetMapping("/admin/cash/ticket/{bookingId}")
    public ResponseEntity<ApiResponse<BusTicketResponse>> getCashTicket(@PathVariable Long bookingId) {
        return ResponseEntity.ok(ApiResponse.success(
                ticketService.getCashTicketDetailsForAdmin(bookingId, currentUserId())));
    }

    @GetMapping("/admin/schedule/{id}/manifest")
    public ResponseEntity<?> getManifest(@PathVariable Long id) {

        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String adminUserId = jwt.getSubject();

        return ResponseEntity.ok(bookingService.getManifestForSchedule(id, adminUserId));
    }

    private String currentUserId() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return jwt.getSubject();
    }

    private ResponseEntity<?> confirmWithPaymentMethod(ConfirmBookingRequest req) {
        String userId = currentUserId();

        List<SeatBooking> bookings = bookingService.confirmBookingWithPaymentMethod(
                req.getBookingRef(),
                userId,
                req.getPaymentMethod());

        return ResponseEntity.ok(toBookingPaymentResponse(req.getBookingRef(), bookings));
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

    private ResponseEntity<?> directGatewayPaymentResponse(PaymentStatus status, Map<String, Object> body) {
        if (status == PaymentStatus.FAILED) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(body);
        }
        if (status == PaymentStatus.EXPIRED) {
            return ResponseEntity.status(HttpStatus.GONE).body(body);
        }
        return ResponseEntity.ok(body);
    }

    private Map<String, Object> directGatewayPaymentInitiateResponse(
            String bookingRef,
            GatewayPaymentInitiateResponse payment
    ) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("bookingRef", bookingRef);
        response.put("paymentMethod", PAYMENT_METHOD_DIRECT_GATEWAY);
        response.put("paymentRef", payment.getPaymentRef());
        response.put("providerTransactionId", payment.getProviderTransactionId());
        response.put("status", payment.getStatus());
        response.put("amount", payment.getAmount());
        response.put("currency", payment.getCurrency());
        response.put("description", DIRECT_GATEWAY_PAYMENT_DESCRIPTION);
        response.put("responseCode", payment.getResponseCode());
        response.put("responseDesc", payment.getResponseDesc());
        response.put("bankList", payment.getBankList());
        response.put("expiresAt", payment.getExpiresAt());
        return response;
    }
}
