package com.goBhutan.adminPanel.theater.repository;

import com.goBhutan.adminPanel.theater.entity.Seat;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    // Find seats by hall, ordered by row and seat number
    List<Seat> findByHallIdOrderByRowNameAscSeatNumberAsc(Long hallId);

    // Find seats by hall and row
    List<Seat> findByHallIdAndRowNameOrderBySeatNumberAsc(Long hallId, String rowName);

    // Find blocked seats by hall
    List<Seat> findByHallIdAndIsBlockedTrue(Long hallId);

    // Find available seats by hall
    List<Seat> findByHallIdAndIsBlockedFalse(Long hallId);

    // Count seats by hall
    long countByHallId(Long hallId);

    // Count blocked seats by hall
    long countByHallIdAndIsBlockedTrue(Long hallId);

    // Count available seats by hall
    long countByHallIdAndIsBlockedFalse(Long hallId);

    // Check if seat exists by hall, row, and number
    boolean existsByHallIdAndRowNameAndSeatNumber(Long hallId, String rowName, Integer seatNumber);

    // Find seat by hall, row, and number
    Optional<Seat> findByHallIdAndRowNameAndSeatNumber(Long hallId, String rowName, Integer seatNumber);

    // Delete all seats by hall
    void deleteByHallId(Long hallId);

    // Get distinct row names for a hall
    @Query("SELECT DISTINCT s.rowName FROM Seat s WHERE s.hall.id = :hallId ORDER BY s.rowName")
    List<String> findDistinctRowNamesByHallId(@Param("hallId") Long hallId);

    // Count seats by seat class
    @Query("SELECT s.seatClass, COUNT(s) FROM Seat s WHERE s.hall.id = :hallId GROUP BY s.seatClass")
    List<Object[]> countBySeatClassForHall(@Param("hallId") Long hallId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Seat s WHERE s.id = :seatId")
    Optional<Seat> findByIdForUpdate(@Param("seatId") Long seatId);

    // In your existing SeatRepository
    @Query("""
    SELECT s FROM Seat s
    JOIN FETCH s.hall h
    JOIN FETCH s.seatClass sc
    WHERE s.id = :id
    """)
    Optional<Seat> findByIdWithHallAndClass(@Param("id") Long id);

}