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
//import java.util.Optional;
//@Repository
//public interface BookingRepository extends JpaRepository<Booking, String> {
//    Page<Booking> findAllByOrderByBookingDateDesc(Pageable pageable);
//    Page<Booking> findByUserIdOrderByBookingDateDesc(String userId, Pageable pageable);
//    Page<Booking> findByScreeningIdOrderByBookingDateDesc(String screeningId, Pageable pageable);
//    Page<Booking> findByStatusOrderByBookingDateDesc(Booking.BookingStatus status, Pageable pageable);
//
//    List<Booking> findByScreeningIdAndStatus(String screeningId, Booking.BookingStatus status);
//    List<Booking> findByScreeningIdAndSeatId(String screeningId, String seatId);
//
//    Optional<Booking> findByBookingReference(String bookingReference);
//
//    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = 'CONFIRMED'")
//    Long countConfirmedBookings();
//
//    @Query("SELECT COUNT(b) FROM Booking b WHERE DATE(b.bookingDate) = CURRENT_DATE AND b.status = 'CONFIRMED'")
//    Long countTodayBookings();
//
//    @Query("SELECT SUM(b.pricePaid) FROM Booking b WHERE b.status = 'CONFIRMED'")
//    java.math.BigDecimal calculateTotalRevenue();
//
//    @Query("SELECT SUM(b.pricePaid) FROM Booking b WHERE DATE(b.bookingDate) = CURRENT_DATE AND b.status = 'CONFIRMED'")
//    java.math.BigDecimal calculateTodayRevenue();
//
//    @Query("SELECT COUNT(b) FROM Booking b WHERE b.screening.id = :screeningId AND b.seat.id = :seatId AND b.status = 'CONFIRMED'")
//    Long countConfirmedBookingsByScreeningAndSeat(@Param("screeningId") String screeningId, @Param("seatId") String seatId);
//
//    boolean existsByScreeningIdAndSeatIdAndStatus(String screeningId, String seatId, Booking.BookingStatus status);
//}