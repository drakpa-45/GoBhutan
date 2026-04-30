package com.goBhutan.adminPanel.paymentInt.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_wallet_accounts", uniqueConstraints = @UniqueConstraint(columnNames = "user_id"))
@Getter
@Setter
public class WalletAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, length = 120)
    private String userId;

    @Column(name = "wallet_id", unique = true, length = 24)
    private String walletId;

    @Column(nullable = false, length = 10)
    private String currency = "BTN";

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(nullable = false, length = 20)
    private String status = "ACTIVE";

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
