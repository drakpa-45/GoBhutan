package com.goBhutan.adminPanel.busAdmin.repository;

import com.goBhutan.adminPanel.busAdmin.entity.Bus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BusRepository extends JpaRepository<Bus, Long> {
    @Query("SELECT b FROM Bus b WHERE b.adminUserId = :adminUserId AND (b.isActive = true OR b.isActive IS NULL)")
    List<Bus> findByAdminUserId(String adminUserId);

    @Query("SELECT b FROM Bus b WHERE b.isActive = true OR b.isActive IS NULL")
    List<Bus> findActiveBuses();

    Optional<Bus> findByBusNumber(String busNumber);

    @Query("SELECT b FROM Bus b WHERE b.id = :id AND b.adminUserId = :adminUserId AND (b.isActive = true OR b.isActive IS NULL)")
    Optional<Bus> findByIdAndAdminUserId(Long id, String adminUserId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Bus b WHERE b.id = :id AND b.adminUserId = :adminUserId AND (b.isActive = true OR b.isActive IS NULL)")
    Optional<Bus> lockByIdAndAdminUserId(@Param("id") Long id,
                                         @Param("adminUserId") String adminUserId);
}
