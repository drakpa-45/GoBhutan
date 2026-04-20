package com.goBhutan.adminPanel.paymentInt.repository;

import com.goBhutan.adminPanel.paymentInt.entity.PaymentTransaction;
import com.goBhutan.adminPanel.paymentInt.enums.PaymentStatus;
import com.goBhutan.adminPanel.paymentInt.enums.PaymentTransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}
