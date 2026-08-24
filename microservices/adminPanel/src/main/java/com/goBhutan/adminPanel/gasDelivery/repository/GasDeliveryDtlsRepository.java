package com.goBhutan.adminPanel.gasDelivery.repository;

import com.goBhutan.adminPanel.gasDelivery.entity.GasDeliveryDtls;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GasDeliveryDtlsRepository extends JpaRepository<GasDeliveryDtls, Long> {

    @Query("""
            select distinct delivery
            from GasDeliveryDtls delivery
            left join fetch delivery.items item
            left join fetch item.gasConfig
            order by delivery.createdAt desc
            """)
    List<GasDeliveryDtls> findAllByOrderByCreatedAtDesc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<GasDeliveryDtls> findWithLockById(Long id);
}
