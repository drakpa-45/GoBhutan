package com.goBhutan.adminPanel.paymentInt.repository;

import com.goBhutan.adminPanel.paymentInt.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    Optional<PaymentTransaction> findByPaymentRef(String paymentRef);
}

