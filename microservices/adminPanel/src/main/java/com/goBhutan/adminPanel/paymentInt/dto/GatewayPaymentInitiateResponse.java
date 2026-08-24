package com.goBhutan.adminPanel.paymentInt.dto;

import com.goBhutan.adminPanel.paymentInt.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class GatewayPaymentInitiateResponse {
    private String paymentRef;
    private String providerTransactionId;
    private String checkoutUrl;
    private PaymentStatus status;
    private BigDecimal amount;
    private String currency;
    private String responseCode;
    private String responseDesc;
    private List<BfsBankItemResponse> bankList;
    private LocalDateTime expiresAt;
}
