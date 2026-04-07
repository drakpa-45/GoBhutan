package com.goBhutan.adminPanel.paymentInt.dto;

import lombok.Data;

@Data
public class TopupAccountInquiryRequest {
    private String topupRef;
    private String remitterBankId;
    private String remitterAccNo;
}
