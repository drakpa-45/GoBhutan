package com.goBhutan.adminPanel.hotel.repository;

import java.util.List;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.goBhutan.adminPanel.hotel.entity.Room;
import com.goBhutan.adminPanel.hotel.entity.Room.RoomStatus;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByHotelId(Long hotelId);
    List<Room> findByHotelIdAndIsActive(Long hotelId, Boolean isActive);
    List<Room> findByHotelIdAndStatus(Long hotelId, RoomStatus status);
    
    @Query("SELECT COUNT(r) FROM Room r WHERE r.hotel.id = :hotelId AND r.status = :status")
    long countRoomsByHotelAndStatus(@Param("hotelId") Long hotelId, @Param("status") RoomStatus status);
    
    @Query("SELECT r FROM Room r WHERE r.hotel.adminUserId = :adminUserId")
    List<Room> findRoomsByAdminUserId(@Param("adminUserId") String adminUserId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Room r WHERE r.id = :id")
    Room findByIdForUpdate(@Param("id") Long id);

}