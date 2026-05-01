package com.goBhutan.adminPanel.busAdmin.dto;

import lombok.Data;

@Data
public class DirectGatewayPaymentDebitRequest {
    private String paymentRef;
    private String otp;
}
