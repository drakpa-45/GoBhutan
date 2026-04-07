package com.goBhutan.adminPanel.paymentInt.dto;

import com.goBhutan.adminPanel.paymentInt.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TopupAccountInquiryResponse {
    private String topupRef;
    private String providerTransactionId;
    private PaymentStatus status;
    private String responseCode;
    private String responseDesc;
    private boolean otpRequired;
    private String remitterBankId;
    private String remitterAccNoMasked;
}
