package com.goBhutan.adminPanel.theater.repository;

import com.goBhutan.adminPanel.theater.entity.TheaterLocation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TheaterLocationRepository extends JpaRepository<TheaterLocation, Long> {

    // Find all with pagination, ordered by creation date
    Page<TheaterLocation> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // Search by dzongkhag
    List<TheaterLocation> findByDzongkhagContainingIgnoreCase(String dzongkhag);

    // Search by dzongkhag and thromdoe
    List<TheaterLocation> findByDzongkhagAndThromdoeContainingIgnoreCase(String dzongkhag, String thromdoe);

    // Check if location exists
    boolean existsByDzongkhagAndThromdoe(String dzongkhag, String thromdoe);

    // Find by dzongkhag exactly
    List<TheaterLocation> findByDzongkhag(String dzongkhag);

    // Find by thromdoe
    List<TheaterLocation> findByThromdoe(String thromdoe);

    // Search in any field
    @Query("SELECT tl FROM TheaterLocation tl WHERE " +
            "LOWER(tl.dzongkhag) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(tl.thromdoe) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(tl.address) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<TheaterLocation> searchLocations(@Param("keyword") String keyword);
}