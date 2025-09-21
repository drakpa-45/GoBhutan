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
//import java.util.List;
//@Repository
//public interface SeatRepository extends JpaRepository<Seat, String> {
//    Page<Seat> findAllByIsActiveTrueOrderByRowNameAscSeatNumberAsc(Pageable pageable);
//    Page<Seat> findByHallIdAndIsActiveTrueOrderByRowNameAscSeatNumberAsc(String hallId, Pageable pageable);
//    List<Seat> findByHallIdAndIsActiveTrueOrderByRowNameAscSeatNumberAsc(String hallId);
//    List<Seat> findByHallIdAndSeatClassAndIsActiveTrueOrderByRowNameAscSeatNumberAsc(String hallId, Seat.SeatClass seatClass);
//    boolean existsByHallIdAndRowNameAndSeatNumber(String hallId, String rowName, String seatNumber);
//
//    @Query("SELECT COUNT(s) FROM Seat s WHERE s.hall.id = :hallId AND s.isActive = true")
//    Long countByHallIdAndIsActive(@Param("hallId") String hallId);
//
//    @Query("SELECT COUNT(s) FROM Seat s WHERE s.hall.id = :hallId AND s.isBlocked = false AND s.isActive = true")
//    Long countAvailableSeatsByHallId(@Param("hallId") String hallId);
//
//    @Query("SELECT COUNT(s) FROM Seat s WHERE s.isBlocked = true AND s.isActive = true")
//    Long countBlockedSeats();
//}