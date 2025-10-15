package com.goBhutan.adminPanel.theater.repository;

import com.goBhutan.adminPanel.theater.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface MovieCategoryRepository extends JpaRepository<MovieCategory, String> {
    Page<MovieCategory> findAllByIsActiveTrueOrderByCreatedAtDesc(Pageable pageable);
    List<MovieCategory> findByIsActiveTrueOrderByNameAsc();
    boolean existsByNameIgnoreCase(String name);
    Optional<MovieCategory> findByNameIgnoreCase(String name);

    @Query("SELECT COUNT(mc) FROM MovieCategory mc WHERE mc.isActive = true")
    Long countActiveCategories();
}
