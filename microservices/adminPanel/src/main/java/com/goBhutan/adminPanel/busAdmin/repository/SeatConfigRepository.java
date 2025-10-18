package com.goBhutan.adminPanel.busAdmin.repository;

import com.goBhutan.adminPanel.busAdmin.entity.SeatConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatConfigRepository extends JpaRepository<SeatConfig,Long> {
    List<SeatConfig> findByBus_Id(Long busId);
}
