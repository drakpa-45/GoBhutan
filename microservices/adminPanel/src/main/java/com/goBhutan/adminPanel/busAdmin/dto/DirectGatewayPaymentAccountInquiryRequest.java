package com.goBhutan.adminPanel.busAdmin.dto;

import lombok.Data;

@Data
public class DirectGatewayPaymentAccountInquiryRequest {
    private String paymentRef;
    private String remitterBankId;
    private String remitterAccNo;
}
