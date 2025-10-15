package com.goBhutan.adminPanel.theater.repository;

import com.goBhutan.adminPanel.theater.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TheaterLocationRepository extends JpaRepository<TheaterLocation, String> {
    Page<TheaterLocation> findAllByOrderByCreatedAtDesc(Pageable pageable);
    List<TheaterLocation> findByDzongkhagContainingIgnoreCase(String dzongkhag);
    List<TheaterLocation> findByDzongkhagAndThromdoeContainingIgnoreCase(String dzongkhag, String thromdoe);
    boolean existsByDzongkhagAndThromdoeAndTown(String dzongkhag, String thromdoe, String town);
}
