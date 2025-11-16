package com.goBhutan.adminPanel.busAdmin.repository;

import com.goBhutan.adminPanel.busAdmin.entity.Bus;
import com.goBhutan.adminPanel.busAdmin.entity.BusRouteMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BusRouteMapRepository extends JpaRepository<BusRouteMap, Long> {
    List<BusRouteMap> findByBusAndActiveTrue(Bus bus);
    List<BusRouteMap> findByBus_IdAndBus_AdminUserId(Long busId, String adminUserId);
    Optional<BusRouteMap> findByIdAndBus_AdminUserId(Long id, String adminUserId);
}
