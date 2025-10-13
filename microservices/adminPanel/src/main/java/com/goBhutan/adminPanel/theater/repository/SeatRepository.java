package com.goBhutan.adminPanel.theater.repository;

import com.goBhutan.adminPanel.theater.entity.Seat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, String> {

    // --- Existing methods ---
    Page<Seat> findAllByIsActiveTrueOrderByRowNameAscSeatNumberAsc(Pageable pageable);
    Page<Seat> findByHallIdAndIsActiveTrueOrderByRowNameAscSeatNumberAsc(String hallId, Pageable pageable);
    List<Seat> findByHallIdAndIsActiveTrueOrderByRowNameAscSeatNumberAsc(String hallId);
    List<Seat> findByHallIdAndSeatClassAndIsActiveTrueOrderByRowNameAscSeatNumberAsc(String hallId, Seat.SeatClass seatClass);
    boolean existsByHallIdAndRowNameAndSeatNumber(String hallId, String rowName, String seatNumber);

    @Query("SELECT COUNT(s) FROM Seat s WHERE s.hall.id = :hallId AND s.isActive = true")
    Long countByHallIdAndIsActive(@Param("hallId") String hallId);

    @Query("SELECT COUNT(s) FROM Seat s WHERE s.hall.id = :hallId AND s.isBlocked = false AND s.isActive = true")
    Long countAvailableSeatsByHallId(@Param("hallId") String hallId);

    @Query("SELECT COUNT(s) FROM Seat s WHERE s.isBlocked = true AND s.isActive = true")
    Long countBlockedSeats();

    // --- Additional methods for Theater statistics ---

    /** Count total seats by theater ID */
    @Query("SELECT COUNT(s) FROM Seat s WHERE s.hall.theater.id = :theaterId AND s.isActive = true")
    Long countByTheaterIdAndIsActive(@Param("theaterId") String theaterId);

    /** ✅ FIXED: Count seats by theater admin user (not owner) */
    @Query("SELECT COUNT(s) FROM Seat s WHERE s.hall.theater.adminUserId = :adminUserId AND s.isActive = true")
    Long countByTheaterAdminUserIdAndIsActive(@Param("adminUserId") String adminUserId);

    /** Count seats by seat class in a hall */
    @Query("SELECT COUNT(s) FROM Seat s WHERE s.hall.id = :hallId AND s.seatClass = :seatClass AND s.isActive = true")
    Long countByHallIdAndSeatClassAndIsActive(@Param("hallId") String hallId, @Param("seatClass") Seat.SeatClass seatClass);

    /** Count blocked seats by hall */
    @Query("SELECT COUNT(s) FROM Seat s WHERE s.hall.id = :hallId AND s.isBlocked = true AND s.isActive = true")
    Long countBlockedSeatsByHallId(@Param("hallId") String hallId);

    /** Get seat class distribution for a hall */
    @Query("SELECT s.seatClass, COUNT(s) FROM Seat s WHERE s.hall.id = :hallId AND s.isActive = true GROUP BY s.seatClass")
    List<Object[]> getSeatClassDistributionByHallId(@Param("hallId") String hallId);

    /** Find seats by row name in a hall */
    @Query("SELECT s FROM Seat s WHERE s.hall.id = :hallId AND s.rowName = :rowName AND s.isActive = true ORDER BY s.seatNumber ASC")
    List<Seat> findByHallIdAndRowNameAndIsActiveTrue(@Param("hallId") String hallId, @Param("rowName") String rowName);

    /** Get all distinct row names in a hall */
    @Query("SELECT DISTINCT s.rowName FROM Seat s WHERE s.hall.id = :hallId AND s.isActive = true ORDER BY s.rowName ASC")
    List<String> findDistinctRowNamesByHallId(@Param("hallId") String hallId);

    /** Find seats by theater ID (all halls) */
    @Query("SELECT s FROM Seat s WHERE s.hall.theater.id = :theaterId AND s.isActive = true ORDER BY s.hall.name, s.rowName, s.seatNumber")
    Page<Seat> findByTheaterIdAndIsActiveTrue(@Param("theaterId") String theaterId, Pageable pageable);

    /** Find blocked seats by hall */
    @Query("SELECT s FROM Seat s WHERE s.hall.id = :hallId AND s.isBlocked = true AND s.isActive = true ORDER BY s.rowName, s.seatNumber")
    List<Seat> findBlockedSeatsByHallId(@Param("hallId") String hallId);

    /** Find available (not blocked) seats by hall */
    @Query("SELECT s FROM Seat s WHERE s.hall.id = :hallId AND s.isBlocked = false AND s.isActive = true ORDER BY s.rowName, s.seatNumber")
    List<Seat> findAvailableSeatsByHallId(@Param("hallId") String hallId);

    /** Count seats by theater and seat class */
    @Query("SELECT COUNT(s) FROM Seat s WHERE s.hall.theater.id = :theaterId AND s.seatClass = :seatClass AND s.isActive = true")
    Long countByTheaterIdAndSeatClassAndIsActive(@Param("theaterId") String theaterId, @Param("seatClass") Seat.SeatClass seatClass);

    /** Batch check if seats exist */
    @Query("SELECT s.id FROM Seat s WHERE s.id IN :seatIds AND s.isActive = true")
    List<String> findExistingSeatIds(@Param("seatIds") List<String> seatIds);

    /** Find seats in a specific range */
    @Query("SELECT s FROM Seat s WHERE s.hall.id = :hallId AND s.rowName BETWEEN :startRow AND :endRow AND s.isActive = true ORDER BY s.rowName, s.seatNumber")
    List<Seat> findSeatsByRowRange(@Param("hallId") String hallId, @Param("startRow") String startRow, @Param("endRow") String endRow);

    /** Count total seats across all theaters */
    @Query("SELECT COUNT(s) FROM Seat s WHERE s.isActive = true")
    Long countAllActiveSeats();

    /** Get seat statistics by theater */
    @Query("SELECT s.hall.theater.id, s.hall.theater.name, COUNT(s), SUM(CASE WHEN s.isBlocked = true THEN 1 ELSE 0 END) " +
            "FROM Seat s WHERE s.isActive = true GROUP BY s.hall.theater.id, s.hall.theater.name")
    List<Object[]> getSeatStatisticsByTheater();

    /** Soft delete (deactivate) seats by hall ID */
    @Query("UPDATE Seat s SET s.isActive = false WHERE s.hall.id = :hallId")
    void deactivateSeatsByHallId(@Param("hallId") String hallId);
}
