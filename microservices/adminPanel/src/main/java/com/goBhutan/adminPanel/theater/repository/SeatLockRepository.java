package com.goBhutan.adminPanel.theater.repository;

import com.goBhutan.adminPanel.theater.entity.SeatLock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SeatLockRepository extends JpaRepository<SeatLock, Long> {

    // Active lock for a specific seat+showtime (any user)
    @Query("""
        SELECT sl FROM SeatLock sl
        WHERE sl.seat.id = :seatId
          AND sl.screenId = :screenId
          AND sl.expiresAt > :now
        """)
    Optional<SeatLock> findActiveLock(
            @Param("seatId") Long seatId,
            @Param("screenId") Long screenId,
            @Param("now") LocalDateTime now
    );

    // Active lock by specific user
    @Query("""
        SELECT sl FROM SeatLock sl
        WHERE sl.seat.id = :seatId
          AND sl.screenId = :screenId
          AND sl.lockedByUserId = :userId
          AND sl.expiresAt > :now
        """)
    Optional<SeatLock> findActiveLockByUser(
            @Param("seatId") Long seatId,
            @Param("screenId") Long screenId,
            @Param("userId") String userId,
            @Param("now") LocalDateTime now
    );

    // All active locks for a showtime + hall (for seat map rendering)
    @Query("""
        SELECT sl FROM SeatLock sl
        JOIN FETCH sl.seat s
        JOIN FETCH s.seatClass sc
        WHERE sl.screenId = :screenId
          AND s.hall.id = :hallId
          AND sl.expiresAt > :now
        """)
    List<SeatLock> findAllActiveByShowtimeAndHall(
            @Param("screenId") Long screenId,
            @Param("hallId") Long hallId,
            @Param("now") LocalDateTime now
    );

    // Active locks for a showtime + hall + specific seatClass (class-level view)
    @Query("""
        SELECT sl FROM SeatLock sl
        JOIN FETCH sl.seat s
        WHERE sl.screenId = :screenId
          AND s.hall.id = :hallId
          AND s.seatClass.id = :seatClassId
          AND sl.expiresAt > :now
        """)
    List<SeatLock> findAllActiveByShowtimeHallAndClass(
            @Param("screenId") Long screenId,
            @Param("hallId") Long hallId,
            @Param("seatClassId") Long seatClassId,
            @Param("now") LocalDateTime now
    );

    // Booking guard: validate user holds locks for specific seats
    @Query("""
        SELECT sl FROM SeatLock sl
        WHERE sl.screenId = :screenId
          AND sl.lockedByUserId = :userId
          AND sl.seat.id IN :seatIds
          AND sl.expiresAt > :now
        """)
    List<SeatLock> findActiveLocksForUserSeats(
            @Param("screenId") Long screenId,
            @Param("userId") String userId,
            @Param("seatIds") List<Long> seatIds,
            @Param("now") LocalDateTime now
    );

    @Modifying
    @Transactional
    @Query("DELETE FROM SeatLock sl WHERE sl.expiresAt <= :now")
    int deleteExpiredLocks(@Param("now") LocalDateTime now);
}