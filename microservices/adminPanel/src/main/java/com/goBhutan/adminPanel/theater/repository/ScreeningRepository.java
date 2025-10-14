package com.goBhutan.adminPanel.theater.repository;

import com.goBhutan.adminPanel.theater.entity.Screening;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.Instant;
import java.util.List;

@Repository
public interface ScreeningRepository extends JpaRepository<Screening, String> {

    // Basic finders
    Page<Screening> findAllByIsActiveTrueOrderByScreeningDateDescStartTimeDesc(Pageable pageable);
    Page<Screening> findByMovieIdAndIsActiveTrueOrderByScreeningDateDescStartTimeDesc(String movieId, Pageable pageable);
    Page<Screening> findByHallIdAndIsActiveTrueOrderByScreeningDateDescStartTimeDesc(String hallId, Pageable pageable);
    Page<Screening> findByScreeningDateAndIsActiveTrueOrderByStartTimeDesc(LocalDate screeningDate, Pageable pageable);

    List<Screening> findByHallIdAndScreeningDateAndIsActiveTrue(String hallId, LocalDate screeningDate);

    List<Screening> findByMovieIdAndScreeningDateGreaterThanEqualAndIsActiveTrueOrderByScreeningDateAscStartTimeAsc(
            String movieId, LocalDate fromDate);

    // Conflict check
    @Query("""
            SELECT s FROM Screening s
            WHERE s.hall.id = :hallId AND s.screeningDate = :date
              AND ((s.startTime <= :endTime AND s.endTime >= :startTime))
              AND s.isActive = true
            """)
    List<Screening> findConflictingScreenings(@Param("hallId") String hallId,
                                              @Param("date") LocalDate date,
                                              @Param("startTime") Instant startTime,
                                              @Param("endTime") Instant endTime);

    // Counts and statistics
    @Query("SELECT COUNT(s) FROM Screening s WHERE s.isActive = true")
    Long countActiveScreenings();

    @Query("SELECT COUNT(s) FROM Screening s WHERE s.hall.theater.adminUserId = :adminUserId AND s.isActive = true")
    Long countActiveScreeningsByAdminUserId(@Param("adminUserId") String adminUserId);

    @Query("SELECT COUNT(s) FROM Screening s WHERE s.hall.theater.id = :theaterId AND s.isActive = true")
    Long countByTheaterIdAndIsActive(@Param("theaterId") String theaterId);

    @Query("""
            SELECT AVG(CAST(s.bookedSeats AS double) / s.availableSeats * 100)
            FROM Screening s
            WHERE s.isActive = true AND s.availableSeats > 0
            """)
    Double calculateAverageOccupancyRate();

    // Theater-level queries
    @Query("""
            SELECT s FROM Screening s
            WHERE s.hall.theater.id = :theaterId AND s.isActive = true
            ORDER BY s.screeningDate DESC, s.startTime DESC
            """)
    Page<Screening> findByTheaterIdAndIsActiveTrue(@Param("theaterId") String theaterId, Pageable pageable);

    // Time-based queries
    @Query("""
            SELECT s FROM Screening s
            WHERE s.screeningDate >= :fromDate AND s.isActive = true
            ORDER BY s.screeningDate ASC, s.startTime ASC
            """)
    Page<Screening> findUpcomingScreenings(@Param("fromDate") LocalDate fromDate, Pageable pageable);

    @Query("""
            SELECT s FROM Screening s
            WHERE s.screeningDate < :toDate AND s.isActive = true
            ORDER BY s.screeningDate DESC, s.startTime DESC
            """)
    Page<Screening> findPastScreenings(@Param("toDate") LocalDate toDate, Pageable pageable);

    @Query("""
            SELECT s FROM Screening s
            WHERE s.screeningDate = :today AND s.isActive = true
            ORDER BY s.startTime ASC
            """)
    List<Screening> findTodayScreenings(@Param("today") LocalDate today);

    @Query("""
            SELECT s FROM Screening s
            WHERE s.screeningDate BETWEEN :startDate AND :endDate AND s.isActive = true
            ORDER BY s.screeningDate ASC, s.startTime ASC
            """)
    List<Screening> findByDateRange(@Param("startDate") LocalDate startDate,
                                    @Param("endDate") LocalDate endDate);

    @Query("""
            SELECT s FROM Screening s
            WHERE s.movie.id = :movieId AND s.screeningDate BETWEEN :startDate AND :endDate
              AND s.isActive = true
            ORDER BY s.screeningDate ASC, s.startTime ASC
            """)
    List<Screening> findByMovieIdAndDateRange(@Param("movieId") String movieId,
                                              @Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);

    // Occupancy stats
    @Query("""
            SELECT AVG(CAST(s.bookedSeats AS double) / s.availableSeats * 100)
            FROM Screening s
            WHERE s.movie.id = :movieId AND s.isActive = true AND s.availableSeats > 0
            """)
    Double calculateOccupancyRateByMovie(@Param("movieId") String movieId);

    @Query("""
            SELECT AVG(CAST(s.bookedSeats AS double) / s.availableSeats * 100)
            FROM Screening s
            WHERE s.hall.theater.id = :theaterId AND s.isActive = true AND s.availableSeats > 0
            """)
    Double calculateOccupancyRateByTheater(@Param("theaterId") String theaterId);

    @Query("""
            SELECT AVG(CAST(s.bookedSeats AS double) / s.availableSeats * 100)
            FROM Screening s
            WHERE s.hall.id = :hallId AND s.isActive = true AND s.availableSeats > 0
            """)
    Double calculateOccupancyRateByHall(@Param("hallId") String hallId);

    // Statistics
    @Query("""
            SELECT s.movie.id, s.movie.title, COUNT(s), SUM(s.bookedSeats),
                   AVG(CAST(s.bookedSeats AS double) / s.availableSeats * 100)
            FROM Screening s
            WHERE s.isActive = true
            GROUP BY s.movie.id, s.movie.title
            ORDER BY COUNT(s) DESC
            """)
    List<Object[]> getScreeningStatisticsByMovie();

    @Query("""
            SELECT s.hall.theater.id, s.hall.theater.name, COUNT(s), SUM(s.bookedSeats),
                   AVG(CAST(s.bookedSeats AS double) / s.availableSeats * 100)
            FROM Screening s
            WHERE s.isActive = true
            GROUP BY s.hall.theater.id, s.hall.theater.name
            ORDER BY COUNT(s) DESC
            """)
    List<Object[]> getScreeningStatisticsByTheater();

    @Query("""
            SELECT HOUR(s.startTime), COUNT(s),
                   AVG(CAST(s.bookedSeats AS double) / s.availableSeats * 100)
            FROM Screening s
            WHERE s.isActive = true
            GROUP BY HOUR(s.startTime)
            ORDER BY AVG(CAST(s.bookedSeats AS double) / s.availableSeats * 100) DESC
            """)
    List<Object[]> getMostPopularScreeningTimes();

    // Revenue (if booking entity exists)
    @Query("""
            SELECT s.id, s.movie.title, SUM(b.pricePaid)
            FROM Screening s
            LEFT JOIN TheaterBooking b ON b.screening.id = s.id
            WHERE s.isActive = true AND b.status = 'CONFIRMED'
            GROUP BY s.id, s.movie.title
            ORDER BY SUM(b.pricePaid) DESC
            """)
    List<Object[]> getRevenueByScreening();

    @Query("""
            SELECT SUM(b.pricePaid)
            FROM Screening s
            LEFT JOIN TheaterBooking b ON b.screening.id = s.id
            WHERE s.screeningDate BETWEEN :startDate AND :endDate
              AND s.isActive = true AND b.status = 'CONFIRMED'
            """)
    java.math.BigDecimal getRevenueByDateRange(@Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);

    // Future screenings
    @Query("""
            SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END
            FROM Screening s
            WHERE s.movie.id = :movieId AND s.screeningDate >= :fromDate AND s.isActive = true
            """)
    Boolean hasUpcomingScreenings(@Param("movieId") String movieId, @Param("fromDate") LocalDate fromDate);

    @Query("""
            SELECT s FROM Screening s
            WHERE s.movie.id = :movieId AND s.screeningDate >= :fromDate AND s.isActive = true
            ORDER BY s.screeningDate ASC, s.startTime ASC
            """)
    List<Screening> findNextScreeningsForMovie(@Param("movieId") String movieId,
                                               @Param("fromDate") LocalDate fromDate,
                                               Pageable pageable);
}
