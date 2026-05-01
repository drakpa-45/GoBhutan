package com.goBhutan.adminPanel.busAdmin.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DirectGatewayPaymentInitiateRequest {
    private String bookingRef;
    private BigDecimal amount;
    private String currency;
    private String description;
    private String remitterEmail;
}
