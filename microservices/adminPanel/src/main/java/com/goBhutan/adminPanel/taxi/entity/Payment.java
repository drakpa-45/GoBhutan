package com.goBhutan.adminPanel.taxi.entity;

import com.goBhutan.adminPanel.taxi.enums.TaxiPaymentMethod;
import com.goBhutan.adminPanel.taxi.enums.TaxiPaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * One row per payment event on a booking.
 * A Reserved booking may have 2 rows: DEPOSIT + BALANCE.
 * A Pull booking typically has 1 row: FULL_PAYMENT.
 */
@Entity
@Table(name = "tbl_taxi_payment")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Column(name = "payer_id", nullable = false)
    private Long payerId;

    /**
     * DEPOSIT or BALANCE or FULL_PAYMENT or REFUND
     */
    @Column(name = "payment_type", nullable = false)
    private String paymentType;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private TaxiPaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private TaxiPaymentStatus paymentStatus;

    /** Reference from mPay / BOB / BNB gateway response */
    @Column(name = "gateway_reference")
    private String gatewayReference;

    /** For cash: driver marks received via app */
    @Column(name = "cash_confirmed_by_driver")
    private Boolean cashConfirmedByDriver = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "settled_at")
    private LocalDateTime settledAt;

    @PrePersist
    public void onCreate() { this.createdAt = LocalDateTime.now(); }

    public static final String TYPE_DEPOSIT      = "DEPOSIT";
    public static final String TYPE_BALANCE      = "BALANCE";
    public static final String TYPE_FULL_PAYMENT = "FULL_PAYMENT";
    public static final String TYPE_REFUND       = "REFUND";
}
