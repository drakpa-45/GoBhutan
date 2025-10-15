package com.goBhutan.adminPanel.theater.repository;

import com.goBhutan.adminPanel.theater.entity.Theater;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TheaterRepository extends JpaRepository<Theater, String> {

    // 🔹 For all theaters
    Page<Theater> findAllByIsActiveTrueOrderByCreatedAtDesc(Pageable pageable);
    List<Theater> findAllByIsActiveTrueOrderByNameAsc();

    // 🔹 For specific admin user (previously ownerId → now adminUserId)
    Page<Theater> findByAdminUserIdAndIsActiveTrueOrderByCreatedAtDesc(String adminUserId, Pageable pageable);
    List<Theater> findByAdminUserIdAndIsActiveTrueOrderByNameAsc(String adminUserId);

    // 🔹 For specific location
    Page<Theater> findByLocationIdAndIsActiveTrueOrderByCreatedAtDesc(String locationId, Pageable pageable);
    List<Theater> findByLocationIdAndIsActiveTrueOrderByNameAsc(String locationId);

    // 🔹 For filtering by Dzongkhag name
    Page<Theater> findByLocationDzongkhagContainingIgnoreCaseAndIsActiveTrueOrderByCreatedAtDesc(
            String dzongkhag, Pageable pageable);

    // 🔹 For filtering by Theater name
    Page<Theater> findByNameContainingIgnoreCaseAndIsActiveTrueOrderByCreatedAtDesc(
            String name, Pageable pageable);

    // 🔹 For internal usage
    List<Theater> findByAdminUserId(String adminUserId);

    // 🔹 Validation checks
    boolean existsByNameIgnoreCaseAndLocationId(String name, String locationId);

    // 🔹 Count active theaters
    @Query("SELECT COUNT(t) FROM Theater t WHERE t.isActive = true")
    Long countActiveTheaters();
}
