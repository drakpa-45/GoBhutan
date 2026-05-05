package com.goBhutan.adminPanel.paymentInt.dto;

import lombok.Data;

@Data
public class DirectPaymentDebitRequest {
    private String paymentRef;
    private String otp;
}
