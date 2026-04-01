package com.goBhutan.adminPanel.theater.repository;

import com.goBhutan.adminPanel.theater.entity.TheaterBookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookingStatusRepository
        extends JpaRepository<TheaterBookingStatus, Long> {

    Optional<TheaterBookingStatus> findByStatusNameIgnoreCase(String statusName);
}

