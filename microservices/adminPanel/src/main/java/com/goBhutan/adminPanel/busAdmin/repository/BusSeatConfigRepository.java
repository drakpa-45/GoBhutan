package com.goBhutan.adminPanel.busAdmin.repository;

import com.goBhutan.adminPanel.busAdmin.entity.BusSeatConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BusSeatConfigRepository extends JpaRepository<BusSeatConfig,Long> {
    List<BusSeatConfig> findByBus_Id(Long busId);
}
