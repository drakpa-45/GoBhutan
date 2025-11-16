package com.goBhutan.adminPanel.busAdmin.repository;

import com.goBhutan.adminPanel.busAdmin.entity.Bus;
import com.goBhutan.adminPanel.busAdmin.entity.Route;
import com.goBhutan.adminPanel.busAdmin.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BusScheduleRepository extends JpaRepository<Schedule, Long> {

    List<Schedule> findByBus_AdminUserId(String adminUserId);
    List<Schedule> findByBus_IdAndBus_AdminUserId(Long busId, String adminUserId);
    List<Schedule> findByRoute_IdAndBus_AdminUserId(Long routeId, String adminUserId);
    Optional<Schedule> findByIdAndBus_AdminUserId(Long scheduleId, String adminUserId);
    List<Schedule> findByBus_AdminUserIdAndDepartureTimeBetween(String adminUserId, LocalDateTime start, LocalDateTime end);

    boolean existsByBusAndRouteAndDepartureTimeBetween(Bus bus, Route route, LocalDateTime localDateTime, LocalDateTime localDateTime1);
}