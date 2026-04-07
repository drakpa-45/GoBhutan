package com.goBhutan.adminPanel.paymentInt.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class InitiatePaymentRequest {
    private String paymentRef;
    private BigDecimal amount;
    private String currency;
    private String description;
}

