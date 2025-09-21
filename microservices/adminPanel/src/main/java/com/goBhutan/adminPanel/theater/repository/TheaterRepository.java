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
//public interface TheaterRepository extends JpaRepository<Theater, String> {
//    Page<Theater> findAllByIsActiveTrueOrderByCreatedAtDesc(Pageable pageable);
//    Page<Theater> findByOwnerIdAndIsActiveTrueOrderByCreatedAtDesc(String ownerId, Pageable pageable);
//    Page<Theater> findByLocationIdAndIsActiveTrueOrderByCreatedAtDesc(String locationId, Pageable pageable);
//    List<Theater> findByOwnerId(String ownerId);
//    boolean existsByNameIgnoreCaseAndLocationId(String name, String locationId);
//
//    @Query("SELECT COUNT(t) FROM Theater t WHERE t.isActive = true")
//    Long countActiveTheaters();
//}