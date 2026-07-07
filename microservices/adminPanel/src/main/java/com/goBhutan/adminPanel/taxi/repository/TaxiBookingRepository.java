package com.goBhutan.adminPanel.taxi.repository;

import com.goBhutan.adminPanel.taxi.entity.TaxiBooking;
import com.goBhutan.adminPanel.taxi.enums.TaxiBookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TaxiBookingRepository extends JpaRepository<TaxiBooking, Long> {
    List<TaxiBooking> findByPassengerIdOrderByCreatedAtDesc(String passengerId);
    List<TaxiBooking> findByDriverIdAndBookingStatusIn(String driverId, List<TaxiBookingStatus> statuses);
    List<TaxiBooking> findByInterRouteId(Long routeId);
}
