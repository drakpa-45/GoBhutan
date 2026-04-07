package com.goBhutan.adminPanel.paymentInt.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class WalletBalanceResponse {
    private String userId;
    private String currency;
    private BigDecimal balance;
    private String status;
}

