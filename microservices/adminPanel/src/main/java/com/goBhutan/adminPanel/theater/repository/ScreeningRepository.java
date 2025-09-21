//package com.goBhutan.adminPanel.theater.repository;
//
//import com.goBhutan.adminPanel.theater.entity.*;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//
//import java.time.LocalDate;
//import java.time.Instant;
//import java.util.List;
//@Repository
//public interface ScreeningRepository extends JpaRepository<Screening, String> {
//    Page<Screening> findAllByIsActiveTrueOrderByScreeningDateDescStartTimeDesc(Pageable pageable);
//    Page<Screening> findByMovieIdAndIsActiveTrueOrderByScreeningDateDescStartTimeDesc(String movieId, Pageable pageable);
//    Page<Screening> findByHallIdAndIsActiveTrueOrderByScreeningDateDescStartTimeDesc(String hallId, Pageable pageable);
//    Page<Screening> findByScreeningDateAndIsActiveTrueOrderByStartTimeDesc(LocalDate screeningDate, Pageable pageable);
//
//    List<Screening> findByHallIdAndScreeningDateAndIsActiveTrue(String hallId, LocalDate screeningDate);
//    List<Screening> findByMovieIdAndScreeningDateGreaterThanEqualAndIsActiveTrueOrderByScreeningDateAscStartTimeAsc(
//            String movieId, LocalDate fromDate);
//
//    @Query("SELECT s FROM Screening s WHERE s.hall.id = :hallId AND s.screeningDate = :date " +
//            "AND ((s.startTime <= :endTime AND s.endTime >= :startTime)) AND s.isActive = true")
//    List<Screening> findConflictingScreenings(@Param("hallId") String hallId,
//                                              @Param("date") LocalDate date,
//                                              @Param("startTime") Instant startTime,
//                                              @Param("endTime") Instant endTime);
//
//    @Query("SELECT COUNT(s) FROM Screening s WHERE s.isActive = true")
//    Long countActiveScreenings();
//
//    @Query("SELECT COUNT(s) FROM Screening s WHERE s.screeningDate = :date AND s.isActive = true")
//    Long countByScreeningDateAndIsActive(@Param("date") LocalDate date);
//
//    @Query("SELECT AVG(CAST(s.bookedSeats AS double) / s.availableSeats * 100) FROM Screening s WHERE s.isActive = true AND s.availableSeats > 0")
//    Double calculateAverageOccupancyRate();
//}