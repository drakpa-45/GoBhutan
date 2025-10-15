package com.goBhutan.adminPanel.theater.repository;

import com.goBhutan.adminPanel.theater.entity.Hall;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HallRepository extends JpaRepository<Hall, String> {
    // Existing methods
    Page<Hall> findAllByIsActiveTrueOrderByCreatedAtDesc(Pageable pageable);
    Page<Hall> findByTheaterIdAndIsActiveTrueOrderByCreatedAtDesc(String theaterId, Pageable pageable);
    List<Hall> findByTheaterId(String theaterId);
    boolean existsByNameIgnoreCaseAndTheaterId(String name, String theaterId);

    @Query("SELECT COUNT(h) FROM Hall h WHERE h.isActive = true")
    Long countActiveHalls();

    @Query("SELECT COUNT(h) FROM Hall h WHERE h.theater.id = :theaterId AND h.isActive = :isActive")
    Long countByTheaterIdAndIsActive(@Param("theaterId") String theaterId, @Param("isActive") Boolean isActive);

    List<Hall> findByTheaterIdAndIsActiveTrueOrderByNameAsc(String theaterId);

    // ✅ Updated methods using adminUserId instead of owner.id
    @Query("SELECT h FROM Hall h WHERE h.theater.adminUserId = :adminUserId AND h.isActive = true ORDER BY h.createdAt DESC")
    Page<Hall> findByTheaterAdminUserIdAndIsActiveTrue(@Param("adminUserId") String adminUserId, Pageable pageable);

    @Query("SELECT COUNT(h) FROM Hall h WHERE h.theater.adminUserId = :adminUserId AND h.isActive = true")
    Long countByTheaterAdminUserIdAndIsActive(@Param("adminUserId") String adminUserId);

    @Query("SELECT h FROM Hall h WHERE h.theater.location.id = :locationId AND h.isActive = true ORDER BY h.createdAt DESC")
    Page<Hall> findByTheaterLocationIdAndIsActiveTrue(@Param("locationId") String locationId, Pageable pageable);

    @Query("SELECT h FROM Hall h WHERE LOWER(h.name) LIKE LOWER(CONCAT('%', :name, '%')) AND h.isActive = true ORDER BY h.name ASC")
    Page<Hall> searchByNameAndIsActiveTrue(@Param("name") String name, Pageable pageable);

    @Query("SELECT h FROM Hall h WHERE h.totalSeats >= :minSeats AND h.isActive = true ORDER BY h.totalSeats ASC")
    List<Hall> findByMinimumSeatsAndIsActiveTrue(@Param("minSeats") Integer minSeats);

    @Query("SELECT SUM(h.totalSeats) FROM Hall h WHERE h.theater.id = :theaterId AND h.isActive = true")
    Long getTotalSeatCapacityByTheaterId(@Param("theaterId") String theaterId);

    boolean existsByTheaterIdAndIsActiveTrue(String theaterId);
}
