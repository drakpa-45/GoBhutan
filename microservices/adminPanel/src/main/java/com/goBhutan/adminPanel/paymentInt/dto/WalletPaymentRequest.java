package com.goBhutan.adminPanel.paymentInt.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WalletPaymentRequest {
    private BigDecimal amount;
    private String currency;
    private String serviceName;
    private String referenceType;
    private String referenceId;
    private String description;
}
