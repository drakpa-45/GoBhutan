package com.goBhutan.adminPanel.theater.repository;

import com.goBhutan.adminPanel.theater.entity.Hall;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HallRepository extends JpaRepository<Hall, Long> {

    // Find active halls only
    List<Hall> findByIsActiveTrue();

    // Find halls by theater
    List<Hall> findByTheaterId(Long theaterId);

    // Find active halls by theater
    List<Hall> findByTheaterIdAndIsActiveTrue(Long theaterId);

    // Find by ID and active status
    Optional<Hall> findByIdAndIsActiveTrue(Long id);

    // Check if hall exists by name and theater
    boolean existsByNameAndTheaterId(String name, Long theaterId);

    // Search halls by name
    @Query("SELECT h FROM Hall h WHERE LOWER(h.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Hall> searchByName(@Param("name") String name);

    // Find halls with pagination
    Page<Hall> findByIsActiveTrue(Pageable pageable);

    // Find halls by theater with pagination
    Page<Hall> findByTheaterId(Long theaterId, Pageable pageable);

    // Count halls by theater
    long countByTheaterId(Long theaterId);

    // Count active halls by theater
    long countByTheaterIdAndIsActiveTrue(Long theaterId);

    // Find halls with seat count
    @Query("SELECT h FROM Hall h LEFT JOIN FETCH h.seats WHERE h.id = :hallId")
    Optional<Hall> findByIdWithSeats(@Param("hallId") Long hallId);

    // Find all halls of a theater with seats
    @Query("SELECT h FROM Hall h LEFT JOIN FETCH h.seats WHERE h.theater.id = :theaterId")
    List<Hall> findByTheaterIdWithSeats(@Param("theaterId") Long theaterId);
}