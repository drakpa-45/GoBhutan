package com.goBhutan.adminPanel.paymentInt.service;

import com.goBhutan.adminPanel.paymentInt.entity.PaymentTransaction;
import com.goBhutan.adminPanel.paymentInt.repository.PaymentTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentTransactionWriter {

    private final PaymentTransactionRepository transactionRepository;

    public PaymentTransactionWriter(PaymentTransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PaymentTransaction saveNew(PaymentTransaction transaction) {
        return transactionRepository.saveAndFlush(transaction);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PaymentTransaction save(PaymentTransaction transaction) {
        return transactionRepository.saveAndFlush(transaction);
    }
}
