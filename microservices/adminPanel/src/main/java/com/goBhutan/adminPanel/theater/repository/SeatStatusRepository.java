package com.goBhutan.adminPanel.theater.repository;

import com.goBhutan.adminPanel.theater.entity.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeatStatusRepository extends JpaRepository<SeatStatus, Long> {
    Optional<SeatStatus> findByStatusNameIgnoreCase(String statusName);
    List<SeatStatus> findByIsActive(Boolean isActive);

}
