package com.goBhutan.adminPanel.taxi.repository;

import com.goBhutan.adminPanel.taxi.entity.RouteStop;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RouteStopRepository extends JpaRepository<RouteStop, Long> {
    List<RouteStop> findByRouteIdOrderByStopSequenceAsc(Long routeId);
}
