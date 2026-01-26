package com.goBhutan.adminPanel.theater.repository;

import com.goBhutan.adminPanel.theater.entity.Theater;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TheaterRepository extends JpaRepository<Theater, Long> {

    // Find active theaters only
    List<Theater> findByIsActiveTrue();

    // Find theaters by location
    List<Theater> findByLocationId(Long locationId);

    // Find active theaters by location
    List<Theater> findByLocationIdAndIsActiveTrue(Long locationId);

    // Find by admin user
    List<Theater> findByAdminUserId(String adminUserId);

    // Search theaters by name
    @Query("SELECT t FROM Theater t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Theater> searchByName(@Param("name") String name);

    // Find with pagination
    Page<Theater> findByIsActiveTrue(Pageable pageable);

    // Check if theater exists by name and location
    boolean existsByNameAndLocationId(String name, Long locationId);

    // Find theater by ID and active status
    Optional<Theater> findByIdAndIsActiveTrue(Long id);
}