package com.goBhutan.adminPanel.paymentInt.controller;

import com.goBhutan.adminPanel.paymentInt.dto.DirectPaymentAccountInquiryRequest;
import com.goBhutan.adminPanel.paymentInt.dto.DirectPaymentDebitRequest;
import com.goBhutan.adminPanel.paymentInt.dto.DirectPaymentInitiateRequest;
import com.goBhutan.adminPanel.paymentInt.enums.PaymentStatus;
import com.goBhutan.adminPanel.paymentInt.service.direct.DirectPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/direct-payment")
@RequiredArgsConstructor
public class DirectPaymentController {

    private final DirectPaymentService directPaymentService;

    @PostMapping("/{module}/initiate")
    public ResponseEntity<?> initiate(@PathVariable String module, @RequestBody DirectPaymentInitiateRequest request) {
        Map<String, Object> response = directPaymentService.initiate(module, request, currentUserId());
        return directPaymentResponse(directPaymentService.statusOf(response), response);
    }

    @PostMapping("/{module}/account-inquiry")
    public ResponseEntity<?> accountInquiry(
            @PathVariable String module,
            @RequestBody DirectPaymentAccountInquiryRequest request
    ) {
        Map<String, Object> response = directPaymentService.accountInquiry(module, request, currentUserId());
        return directPaymentResponse(directPaymentService.statusOf(response), response);
    }

    @PostMapping("/{module}/debit")
    public ResponseEntity<?> debit(@PathVariable String module, @RequestBody DirectPaymentDebitRequest request) {
        Map<String, Object> response = directPaymentService.debit(module, request, currentUserId());
        return directPaymentResponse(directPaymentService.statusOf(response), response);
    }

    @GetMapping("/{module}/status/{paymentRef}")
    public ResponseEntity<?> status(@PathVariable String module, @PathVariable String paymentRef) {
        Map<String, Object> response = directPaymentService.status(module, paymentRef, currentUserId());
        return directPaymentResponse(directPaymentService.statusOf(response), response);
    }

    private ResponseEntity<?> directPaymentResponse(PaymentStatus status, Map<String, Object> body) {
        if (status == PaymentStatus.FAILED) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(body);
        }
        if (status == PaymentStatus.EXPIRED) {
            return ResponseEntity.status(HttpStatus.GONE).body(body);
        }
        return ResponseEntity.ok(body);
    }

    private String currentUserId() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return jwt.getSubject();
    }
}
