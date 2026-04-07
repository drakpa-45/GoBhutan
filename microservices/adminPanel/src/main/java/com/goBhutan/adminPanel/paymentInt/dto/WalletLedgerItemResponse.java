package com.goBhutan.adminPanel.paymentInt.dto;

import com.goBhutan.adminPanel.paymentInt.enums.WalletLedgerType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class WalletLedgerItemResponse {
    private Long id;
    private WalletLedgerType type;
    private BigDecimal amount;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private String referenceType;
    private String referenceId;
    private String remarks;
    private LocalDateTime createdAt;
}

