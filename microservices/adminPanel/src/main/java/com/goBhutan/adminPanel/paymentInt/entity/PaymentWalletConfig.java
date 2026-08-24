package com.goBhutan.adminPanel.paymentInt.entity;

import com.goBhutan.adminPanel.paymentInt.enums.PaymentProvider;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_payment_wallet_config")
@Getter
@Setter
public class PaymentWalletConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private PaymentProvider provider;

    @Column(nullable = false, length = 150)
    private String merchantId;

    @Column(length = 255)
    private String apiKey;

    @Column(length = 255)
    private String apiSecret;

    @Column(nullable = false, length = 500)
    private String callbackUrl;

    @Column(length = 500)
    private String apiUrl;

    @Column(length = 10)
    private String beneficiaryBankCode;

    @Column(length = 10)
    private String version;

    @Column(length = 500)
    private String privateKeyPath;

    @Column(length = 255)
    private String privateKeyPassword;

    @Column(length = 500)
    private String bfsPublicCertPath;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(nullable = false, length = 120)
    private String adminUserId;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
