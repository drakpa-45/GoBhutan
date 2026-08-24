package com.goBhutan.adminPanel.busAdmin.repository;

import com.goBhutan.adminPanel.busAdmin.entity.SeatBooking;
import com.goBhutan.adminPanel.busAdmin.enums.BookingStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SeatBookingRepository extends JpaRepository<SeatBooking, Long> {

    @Modifying
    @Query("""
                UPDATE SeatBooking b
                SET b.status = com.goBhutan.adminPanel.busAdmin.enums.BookingStatus.EXPIRED
                WHERE b.status = com.goBhutan.adminPanel.busAdmin.enums.BookingStatus.LOCKED
                  AND b.lockExpiry IS NOT NULL
                  AND b.lockExpiry < :now
            """)
    int releaseExpiredLocks(LocalDateTime now);

    @Query("""
                SELECT b
                FROM SeatBooking b
                JOIN FETCH b.schedule s
                WHERE b.status = com.goBhutan.adminPanel.busAdmin.enums.BookingStatus.LOCKED
                  AND b.lockExpiry < :now
            """)
    List<SeatBooking> findExpiredLockedSeats(LocalDateTime now);

    List<SeatBooking> findByScheduleId(Long scheduleId);

    Optional<SeatBooking> findByScheduleIdAndSeatNumber(Long scheduleId, Integer seatNumber);

    boolean existsByScheduleIdAndStatus(Long scheduleId, BookingStatus status);

    long countByScheduleIdAndStatus(Long scheduleId, BookingStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
                SELECT b
                FROM SeatBooking b
                WHERE b.bookingRef = :bookingRef
            """)
    List<SeatBooking> findByBookingRefForUpdate(String bookingRef);
}
