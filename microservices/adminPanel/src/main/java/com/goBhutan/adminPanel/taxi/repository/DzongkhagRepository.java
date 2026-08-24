package com.goBhutan.adminPanel.taxi.repository;

import com.goBhutan.adminPanel.taxi.entity.Dzongkhag;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DzongkhagRepository extends JpaRepository<Dzongkhag, Long> {
    List<Dzongkhag> findAllByOrderByNameAsc();
}