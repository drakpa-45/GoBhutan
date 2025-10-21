package com.goBhutan.adminPanel.busAdmin.repository;

import com.goBhutan.adminPanel.busAdmin.entity.BusSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BusSeatRepository extends JpaRepository<BusSeat, Long> {
    List<BusSeat> findByBus_Id(Long busId);
}
