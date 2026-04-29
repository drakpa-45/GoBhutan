package com.goBhutan.adminPanel.paymentInt.entity;

import com.goBhutan.adminPanel.paymentInt.enums.PaymentProvider;
import com.goBhutan.adminPanel.paymentInt.enums.PaymentStatus;
import com.goBhutan.adminPanel.paymentInt.enums.PaymentTransactionType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "tbl_payment_transactions",
        indexes = {
                @Index(name = "idx_payment_ref", columnList = "paymentRef", unique = true),
                @Index(name = "idx_provider_txn_id", columnList = "providerTransactionId"),
                @Index(name = "idx_status", columnList = "status")
        }
)
@Getter
@Setter
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120, unique = true)
    private String paymentRef;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private PaymentProvider provider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private PaymentTransactionType transactionType;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 10)
    private String currency;

    @Column(nullable = false, length = 120)
    private String userId;

    @Column(length = 120)
    private String remitterEmail;

    @Column(length = 500)
    private String description;

    @Column(length = 40)
    private String serviceName;

    @Column(length = 60)
    private String referenceType;

    @Column(length = 120)
    private String referenceId;

    @Column(length = 120)
    private String parentPaymentRef;

    @Column(length = 120)
    private String providerTransactionId;

    @Column(length = 50)
    private String providerResponseCode;

    @Column(length = 500)
    private String providerMessage;

    private LocalDateTime expiresAt;

    private LocalDateTime walletCreditedAt;

    @Column(length = 40)
    private String gatewayOrderNo;

    private LocalDateTime gatewayTransactionTime;

    @Column(length = 20)
    private String remitterBankId;

    @Column(length = 64)
    private String remitterAccNoMasked;

    @Column(length = 120)
    private String remitterName;

    @Column(length = 10)
    private String debitAuthCode;

    @Column(length = 50)
    private String debitAuthNo;

    private LocalDateTime debitRequestedAt;

    @Column(nullable = false)
    private Integer statusCheckCount = 0;

    private LocalDateTime lastStatusCheckedAt;

    @Lob
    private String rawArRequest;

    @Lob
    private String rawRcResponse;

    @Lob
    private String rawAeRequest;

    @Lob
    private String rawEcResponse;

    @Lob
    private String rawDrRequest;

    @Lob
    private String rawAcResponse;

    @Lob
    private String rawAsRequest;

    @Lob
    private String rawAsResponse;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
