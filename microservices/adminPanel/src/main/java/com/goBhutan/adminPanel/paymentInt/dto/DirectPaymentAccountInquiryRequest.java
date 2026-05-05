package com.goBhutan.adminPanel.paymentInt.dto;

import lombok.Data;

@Data
public class DirectPaymentAccountInquiryRequest {
    private String paymentRef;
    private String remitterBankId;
    private String remitterAccNo;
}
