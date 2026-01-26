package com.goBhutan.adminPanel.theater.repository;

import com.goBhutan.adminPanel.theater.entity.SeatClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SeatClassRepository extends JpaRepository<SeatClass, Long> {

    // Optional: find by name (e.g., REGULAR, VIP)
    Optional<SeatClass> findByName(String name);

    // Optional: check if a seat class exists by name
    boolean existsByName(String name);
}
