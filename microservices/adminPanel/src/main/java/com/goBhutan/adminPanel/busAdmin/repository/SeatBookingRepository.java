package com.goBhutan.adminPanel.busAdmin.repository;

import com.goBhutan.adminPanel.busAdmin.entity.Schedule;
import com.goBhutan.adminPanel.busAdmin.entity.SeatBooking;
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

    @Query("""
        SELECT CASE WHEN COUNT(b) > 0 THEN TRUE ELSE FALSE END
        FROM SeatBooking b
        WHERE b.schedule.id = :scheduleId
          AND b.seatNumber = :seat
          AND b.status IN ('LOCKED', 'BOOKED')
          AND (b.lockExpiry IS NULL OR b.lockExpiry > :now)
    """)
    boolean isSeatTaken(Long scheduleId, int seat, LocalDateTime now);

    @Modifying
    @Query("UPDATE SeatBooking b SET b.status='EXPIRED' WHERE b.status='LOCKED' AND b.lockExpiry < :now")
    void releaseExpiredLocks(LocalDateTime now);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Schedule s WHERE s.id = :id")
    Schedule lockSchedule(Long id);

    List<SeatBooking> findByScheduleId(Long scheduleId);
    Optional<SeatBooking> findByPaymentRef(String ref);
}
