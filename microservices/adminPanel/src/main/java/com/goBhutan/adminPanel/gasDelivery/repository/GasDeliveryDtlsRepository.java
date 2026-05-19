package com.goBhutan.adminPanel.gasDelivery.repository;

import com.goBhutan.adminPanel.gasDelivery.entity.GasDeliveryDtls;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GasDeliveryDtlsRepository extends JpaRepository<GasDeliveryDtls, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<GasDeliveryDtls> findWithLockById(Long id);
}
