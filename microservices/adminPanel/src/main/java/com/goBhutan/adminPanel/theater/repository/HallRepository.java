//package com.goBhutan.adminPanel.theater.repository;
//
//import com.goBhutan.adminPanel.theater.entity.*;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//
//@Repository
//public interface HallRepository extends JpaRepository<Hall, String> {
//    Page<Hall> findAllByIsActiveTrueOrderByCreatedAtDesc(Pageable pageable);
//    Page<Hall> findByTheaterIdAndIsActiveTrueOrderByCreatedAtDesc(String theaterId, Pageable pageable);
//    List<Hall> findByTheaterId(String theaterId);
//    boolean existsByNameIgnoreCaseAndTheaterId(String name, String theaterId);
//
//    @Query("SELECT COUNT(h) FROM Hall h WHERE h.isActive = true")
//    Long countActiveHalls();
//}
