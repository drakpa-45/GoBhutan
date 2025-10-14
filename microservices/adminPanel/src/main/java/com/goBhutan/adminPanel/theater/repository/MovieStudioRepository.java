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
public interface MovieStudioRepository extends JpaRepository<MovieStudio, String> {
    Page<MovieStudio> findAllByIsActiveTrueOrderByCreatedAtDesc(Pageable pageable);
    List<MovieStudio> findByIsActiveTrueOrderByNameAsc();
    boolean existsByNameIgnoreCase(String name);
    Optional<MovieStudio> findByNameIgnoreCase(String name);

    @Query("SELECT COUNT(ms) FROM MovieStudio ms WHERE ms.isActive = true")
    Long countActiveStudios();
}
