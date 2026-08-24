package com.goBhutan.adminPanel.paymentInt.repository;

import com.goBhutan.adminPanel.paymentInt.entity.WalletLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WalletLedgerRepository extends JpaRepository<WalletLedger, Long> {
    List<WalletLedger> findTop50ByUserIdOrderByCreatedAtDesc(String userId);
}

