package com.goBhutan.adminPanel.busAdmin.repository;

import com.goBhutan.adminPanel.busAdmin.entity.BookingSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingSeatRepository extends JpaRepository<BookingSeat,Long> {
    List<BookingSeat> findByBookingId(Long bookingId);

    @Query("SELECT bs FROM BookingSeat bs WHERE bs.booking.schedule.id = :scheduleId " +
            "AND bs.booking.status != 'CANCELLED'")
    List<BookingSeat> findBookedSeatsByScheduleId(@Param("scheduleId") Long scheduleId);

    @Query("SELECT bs FROM BookingSeat bs WHERE bs.booking.id = :bookingId AND bs.seatNumber = :seatNumber")
    Optional<BookingSeat> findByBookingIdAndSeatNumber(@Param("bookingId") Long bookingId,
                                                       @Param("seatNumber") String seatNumber);
}
