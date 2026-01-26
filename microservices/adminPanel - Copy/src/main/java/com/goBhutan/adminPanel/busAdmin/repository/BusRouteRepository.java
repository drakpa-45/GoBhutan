package com.goBhutan.adminPanel.busAdmin.repository;

import com.goBhutan.adminPanel.busAdmin.entity.Bus;
import com.goBhutan.adminPanel.busAdmin.entity.BusRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BusRouteRepository extends JpaRepository<BusRoute, Long> {
    // FETCH ROUTES for a bus (admin validated)
    List<BusRoute> findByBus_IdAndBus_AdminUserIdAndActiveTrue(Long busId, String adminUserId);

    // GET ONE ROUTE (admin validated)
    Optional<BusRoute> findByIdAndBus_AdminUserId(Long id, String adminUserId);

    // CHECK DUPLICATE for same time + same source + same destination
    boolean existsByBusAndDepartureTimeAndSourceAndDestinationAndActiveTrue(
            Bus bus,
            LocalTime departureTime,
            String source,
            String destination
    );
}
