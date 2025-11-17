package com.goBhutan.adminPanel.busAdmin.repository;

import com.goBhutan.adminPanel.busAdmin.entity.Bus;
import com.goBhutan.adminPanel.busAdmin.entity.BusSeatConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BusRepository extends JpaRepository<Bus, Long> {
    List<Bus> findByAdminUserId(String adminUserId);
    Optional<Bus> findByBusNumber(String busNumber);
    Optional<Bus> findByIdAndAdminUserId(Long id, String adminUserId);
}
