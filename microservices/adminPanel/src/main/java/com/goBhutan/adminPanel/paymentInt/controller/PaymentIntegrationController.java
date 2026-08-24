package com.goBhutan.adminPanel.paymentInt.controller;

import com.goBhutan.adminPanel.common.dto.ApiResponse;
import com.goBhutan.adminPanel.paymentInt.dto.PaymentStatusResponse;
import com.goBhutan.adminPanel.paymentInt.dto.TopupAccountInquiryRequest;
import com.goBhutan.adminPanel.paymentInt.dto.TopupAccountInquiryResponse;
import com.goBhutan.adminPanel.paymentInt.dto.TopupDebitRequest;
import com.goBhutan.adminPanel.paymentInt.dto.TopupDebitResponse;
import com.goBhutan.adminPanel.paymentInt.dto.TopupInitiateRequest;
import com.goBhutan.adminPanel.paymentInt.dto.TopupInitiateResponse;
import com.goBhutan.adminPanel.paymentInt.dto.WalletBalanceResponse;
import com.goBhutan.adminPanel.paymentInt.dto.WalletConfigResponse;
import com.goBhutan.adminPanel.paymentInt.dto.WalletLedgerItemResponse;
import com.goBhutan.adminPanel.paymentInt.enums.PaymentStatus;
import com.goBhutan.adminPanel.paymentInt.service.PaymentIntegrationService;
import org.springframework.http.HttpStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment-int")
@RequiredArgsConstructor
public class PaymentIntegrationController {

    private final PaymentIntegrationService paymentService;

    @GetMapping("/wallet-config/active")
    public ResponseEntity<ApiResponse<WalletConfigResponse>> getActiveWalletConfig() {
        String adminUserId = currentUserId();
        return ResponseEntity.ok(ApiResponse.success(paymentService.getActiveWalletConfig(adminUserId)));
    }

    @PostMapping("/wallet/topup/initiate")
    public ResponseEntity<ApiResponse<TopupInitiateResponse>> initiateTopup(@RequestBody TopupInitiateRequest req) {
        String userId = currentUserId();
        TopupInitiateResponse response = paymentService.initiateTopup(req, userId);
        if (response.getStatus() == PaymentStatus.FAILED) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(new ApiResponse<>(false,
                            "Topup initiation failed for reference " + response.getTopupRef()
                                    + ": " + response.getResponseDesc(),
                            response));
        }
        return ResponseEntity.ok(ApiResponse.success("Topup initiated", response));
    }

    @PostMapping("/wallet/topup/account-inquiry")
    public ResponseEntity<ApiResponse<TopupAccountInquiryResponse>> verifyTopupAccount(@RequestBody TopupAccountInquiryRequest req) {
        String userId = currentUserId();
        return ResponseEntity.ok(ApiResponse.success("Account inquiry completed",
                paymentService.verifyTopupAccount(req, userId)));
    }

    @PostMapping("/wallet/topup/debit")
    public ResponseEntity<ApiResponse<TopupDebitResponse>> submitTopupOtp(@RequestBody TopupDebitRequest req) {
        String userId = currentUserId();
        return ResponseEntity.ok(ApiResponse.success("Debit request processed",
                paymentService.submitTopupOtp(req, userId)));
    }

    @GetMapping("/wallet/balance")
    public ResponseEntity<ApiResponse<WalletBalanceResponse>> getWalletBalance() {
        String userId = currentUserId();
        return ResponseEntity.ok(ApiResponse.success(paymentService.getWalletBalance(userId)));
    }

    @GetMapping("/wallet/ledger")
    public ResponseEntity<ApiResponse<java.util.List<WalletLedgerItemResponse>>> getWalletLedger() {
        String userId = currentUserId();
        return ResponseEntity.ok(ApiResponse.success(paymentService.getWalletLedger(userId)));
    }

    @GetMapping("/wallet/topup/status/{paymentRef}")
    public ResponseEntity<ApiResponse<PaymentStatusResponse>> getTopupStatus(@PathVariable String paymentRef) {
        String userId = currentUserId();
        return ResponseEntity.ok(ApiResponse.success(paymentService.getTopupStatus(paymentRef, userId)));
    }

    private String currentUserId() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return jwt.getSubject();
    }
}
