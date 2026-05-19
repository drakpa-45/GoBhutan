package com.goBhutan.adminPanel.gasDelivery.repository;

import com.goBhutan.adminPanel.gasDelivery.entity.GasConfigMaster;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GasConfigMasterRepository extends JpaRepository<GasConfigMaster, Long> {

    List<GasConfigMaster> findAllByOrderByGasTypeAsc();

    List<GasConfigMaster> findByActiveTrueOrderByGasTypeAsc();

    Optional<GasConfigMaster> findByIdAndAdminUserId(Long id, String adminUserId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<GasConfigMaster> findByIdAndActiveTrue(Long id);

    boolean existsByAdminUserIdAndGasTypeIgnoreCase(
            String adminUserId,
            String gasType
    );

    boolean existsByAdminUserIdAndGasTypeIgnoreCaseAndIdNot(
            String adminUserId,
            String gasType,
            Long id
    );
}
