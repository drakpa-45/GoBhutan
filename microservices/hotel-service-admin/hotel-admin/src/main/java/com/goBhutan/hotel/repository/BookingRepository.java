package com.goBhutan.hotel.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.goBhutan.hotel.entity.Booking;
import com.goBhutan.hotel.entity.Booking.BookingStatus;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByHotelId(Long hotelId);
    List<Booking> findByHotelIdAndStatus(Long hotelId, BookingStatus status);
    List<Booking> findByRoomId(Long roomId);
    
    @Query("SELECT b FROM Booking b WHERE b.hotel.adminUserId = :adminUserId")
    List<Booking> findBookingsByAdminUserId(@Param("adminUserId") String adminUserId);
    
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.hotel.id = :hotelId AND b.status = :status")
    long countBookingsByHotelAndStatus(@Param("hotelId") Long hotelId, @Param("status") BookingStatus status);
    
    @Query("SELECT b FROM Booking b WHERE b.hotel.id = :hotelId AND b.checkInDate = :date")
    List<Booking> findTodayCheckIns(@Param("hotelId") Long hotelId, @Param("date") LocalDate date);
    
    @Query("SELECT b FROM Booking b WHERE b.hotel.id = :hotelId AND b.checkOutDate = :date")
    List<Booking> findTodayCheckOuts(@Param("hotelId") Long hotelId, @Param("date") LocalDate date);
}