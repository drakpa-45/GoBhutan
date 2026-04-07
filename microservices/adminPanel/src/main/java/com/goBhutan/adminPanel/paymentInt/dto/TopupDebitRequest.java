package com.goBhutan.adminPanel.paymentInt.dto;

import lombok.Data;

@Data
public class TopupDebitRequest {
    private String topupRef;
    private String otp;
}
