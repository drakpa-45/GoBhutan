package com.goBhutan.adminPanel.busAdmin.repository;

import com.goBhutan.adminPanel.busAdmin.entity.BusBookings;
import com.goBhutan.adminPanel.busAdmin.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingsRepo extends JpaRepository<BusBookings, Long> {
    Optional<BusBookings> findByBookingReference(String bookingReference);

    List<BusBookings> findByUserId(String userId);

    List<BusBookings> findByStatus(BookingStatus status);

    @Query("SELECT b FROM BusBookings b WHERE b.schedule.id = :scheduleId AND b.status != 'CANCELLED'")
    List<BusBookings> findActiveBookingsByScheduleId(@Param("scheduleId") Long scheduleId);

    @Query("SELECT b FROM BusBookings b WHERE b.email = :email ORDER BY b.bookingDate DESC")
    List<BusBookings> findByEmail(@Param("email") String email);

    @Query("SELECT b FROM BusBookings b WHERE b.userId = :userId AND b.status = :status")
    List<BusBookings> findByUserIdAndStatus(@Param("userId") String userId, @Param("status") BookingStatus status);
}
