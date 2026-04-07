package com.goBhutan.adminPanel.paymentInt.dto;

import com.goBhutan.adminPanel.paymentInt.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InitiatePaymentResponse {
    private String paymentRef;
    private String providerTransactionId;
    private String checkoutUrl;
    private PaymentStatus status;
}

