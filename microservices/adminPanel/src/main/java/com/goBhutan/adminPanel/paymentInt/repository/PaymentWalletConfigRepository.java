package com.goBhutan.adminPanel.paymentInt.repository;

import com.goBhutan.adminPanel.paymentInt.entity.PaymentWalletConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentWalletConfigRepository extends JpaRepository<PaymentWalletConfig, Long> {
    Optional<PaymentWalletConfig> findFirstByAdminUserIdAndActiveTrueOrderByUpdatedAtDesc(String adminUserId);
    Optional<PaymentWalletConfig> findFirstByActiveTrueOrderByUpdatedAtDesc();
}
