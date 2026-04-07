package com.goBhutan.adminPanel.paymentInt.dto;

import com.goBhutan.adminPanel.paymentInt.enums.PaymentProvider;
import com.goBhutan.adminPanel.paymentInt.enums.PaymentStatus;
import com.goBhutan.adminPanel.paymentInt.enums.PaymentTransactionType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentStatusResponse {
    private String paymentRef;
    private String providerTransactionId;
    private PaymentProvider provider;
    private PaymentTransactionType transactionType;
    private PaymentStatus status;
    private String responseCode;
    private BigDecimal amount;
    private String currency;
    private String message;
    private String remitterBankId;
    private String remitterName;
    private String debitAuthCode;
    private String debitAuthNo;
    private Integer statusCheckCount;
    private LocalDateTime walletCreditedAt;
}
