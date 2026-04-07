package com.goBhutan.adminPanel.paymentInt.entity;

import com.goBhutan.adminPanel.paymentInt.enums.WalletLedgerType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_wallet_ledger", indexes = {
        @Index(name = "idx_wallet_ledger_user", columnList = "userId"),
        @Index(name = "idx_wallet_ledger_ref", columnList = "referenceId")
})
@Getter
@Setter
public class WalletLedger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WalletLedgerType type;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal balanceBefore;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal balanceAfter;

    @Column(nullable = false, length = 50)
    private String referenceType;

    @Column(nullable = false, length = 120)
    private String referenceId;

    @Column(length = 255)
    private String remarks;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}

