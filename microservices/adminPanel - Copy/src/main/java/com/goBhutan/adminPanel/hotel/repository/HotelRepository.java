package com.goBhutan.adminPanel.hotel.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.goBhutan.adminPanel.hotel.entity.Hotel;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {
    List<Hotel> findByAdminUserId(String adminUserId);
    List<Hotel> findByAdminUserIdAndIsActive(String adminUserId, Boolean isActive);
    Optional<Hotel> findByIdAndAdminUserId(Long id, String adminUserId);
    
    @Query("SELECT COUNT(h) FROM Hotel h WHERE h.adminUserId = :adminUserId AND h.isActive = true")
    long countActiveHotelsByAdmin(@Param("adminUserId") String adminUserId);
}
