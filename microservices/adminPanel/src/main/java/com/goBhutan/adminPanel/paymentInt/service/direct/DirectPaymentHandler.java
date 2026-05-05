package com.goBhutan.adminPanel.paymentInt.service.direct;

import com.goBhutan.adminPanel.paymentInt.dto.DirectPaymentInitiateRequest;
import com.goBhutan.adminPanel.paymentInt.dto.ServicePaymentRequest;

import java.time.LocalDateTime;

public interface DirectPaymentHandler {
    String module();

    String referenceType();

    String referenceIdKey();

    default String confirmationKey() {
        return "confirmation";
    }

    ServicePaymentRequest buildPaymentRequest(String referenceId, String userId, DirectPaymentInitiateRequest request);

    void onPaymentPending(String referenceId, String userId, LocalDateTime expiresAt);

    String ensurePaymentCanContinue(String paymentRef, String userId);

    String ensurePaymentCanDebit(String paymentRef, String userId);

    Object confirmPaymentSuccess(String paymentRef, String userId);
}
