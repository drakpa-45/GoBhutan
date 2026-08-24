package com.goBhutan.adminPanel.paymentInt.dto;

import com.goBhutan.adminPanel.paymentInt.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TopupDebitResponse {
    private String topupRef;
    private String providerTransactionId;
    private PaymentStatus status;
    private String responseCode;
    private String responseDesc;
    private String debitAuthCode;
    private String debitAuthNo;
    private String remitterName;
    private String remitterBankId;
}
