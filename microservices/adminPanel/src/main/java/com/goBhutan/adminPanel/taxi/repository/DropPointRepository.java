package com.goBhutan.adminPanel.taxi.repository;

import com.goBhutan.adminPanel.taxi.entity.DropPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DropPointRepository extends JpaRepository<DropPoint, Long> {
    List<DropPoint> findByIsActiveTrue();
}