package com.goBhutan.adminPanel.paymentInt.dto;

import com.goBhutan.adminPanel.paymentInt.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class GatewayPaymentDebitResponse {
    private String paymentRef;
    private String providerTransactionId;
    private PaymentStatus status;
    private BigDecimal amount;
    private String currency;
    private String responseCode;
    private String responseDesc;
    private String debitAuthCode;
    private String debitAuthNo;
    private String remitterName;
    private String remitterBankId;
}
