package com.goBhutan.adminPanel.busAdmin.repository;

import com.goBhutan.adminPanel.busAdmin.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BusRouteRepository extends JpaRepository<Route, Long> {
    List<Route> findByBusId(Long busId);

    Optional<Route> findByIdAndBus_AdminUserId(Long id, String adminUserId);
    @Query("SELECT r FROM Route r WHERE r.bus.adminUserId = :adminUserId")
    List<Route> findByBusAdminUserId(@Param("adminUserId") String adminUserId);

    @Query("SELECT r FROM Route r WHERE r.id = :id AND r.bus.adminUserId = :adminUserId")
    Optional<Route> findByIdAndBusAdminUserId(@Param("id") Long id, @Param("adminUserId") String adminUserId);
}
