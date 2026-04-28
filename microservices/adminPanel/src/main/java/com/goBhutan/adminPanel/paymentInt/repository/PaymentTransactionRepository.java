package com.goBhutan.adminPanel.paymentInt.repository;

import com.goBhutan.adminPanel.paymentInt.entity.PaymentTransaction;
import com.goBhutan.adminPanel.paymentInt.enums.PaymentStatus;
import com.goBhutan.adminPanel.paymentInt.enums.PaymentTransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    Optional<PaymentTransaction> findByPaymentRef(String paymentRef);
    Optional<PaymentTransaction> findByPaymentRefAndUserId(String paymentRef, String userId);
    Optional<PaymentTransaction> findFirstByUserIdAndReferenceTypeAndReferenceIdAndTransactionTypeAndStatus(
            String userId,
            String referenceType,
            String referenceId,
            PaymentTransactionType transactionType,
            PaymentStatus status
    );
    Optional<PaymentTransaction> findFirstByUserIdAndParentPaymentRefAndTransactionTypeAndStatus(
            String userId,
            String parentPaymentRef,
            PaymentTransactionType transactionType,
            PaymentStatus status
    );
    Optional<PaymentTransaction> findFirstByUserIdAndParentPaymentRefAndReferenceTypeAndTransactionTypeAndStatus(
            String userId,
            String parentPaymentRef,
            String referenceType,
            PaymentTransactionType transactionType,
            PaymentStatus status
    );

    @Query("""
            SELECT COALESCE(SUM(p.amount), 0)
            FROM PaymentTransaction p
            WHERE p.userId = :userId
              AND p.parentPaymentRef = :parentPaymentRef
              AND p.transactionType = :transactionType
              AND p.status = :status
            """)
            BigDecimal sumAmountByUserIdAndParentPaymentRefAndTransactionTypeAndStatus(
            String userId,
            String parentPaymentRef,
            PaymentTransactionType transactionType,
            PaymentStatus status
    );

    @Query("""
            SELECT COALESCE(SUM(p.amount), 0)
            FROM PaymentTransaction p
            WHERE p.parentPaymentRef = :parentPaymentRef
              AND p.referenceType = :referenceType
              AND p.transactionType = :transactionType
              AND p.status = :status
            """)
    BigDecimal sumAmountByParentPaymentRefAndReferenceTypeAndTransactionTypeAndStatus(
            String parentPaymentRef,
            String referenceType,
            PaymentTransactionType transactionType,
            PaymentStatus status
    );
}
