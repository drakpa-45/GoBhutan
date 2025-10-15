package com.goBhutan.adminPanel.theater.repository;

import com.goBhutan.adminPanel.theater.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface TheaterBookingRepository extends JpaRepository<TheaterBooking, String> {
    Page<TheaterBooking> findAllByOrderByBookingDateDesc(Pageable pageable);
    Page<TheaterBooking> findByUserIdOrderByBookingDateDesc(String userId, Pageable pageable);
    Page<TheaterBooking> findByScreeningIdOrderByBookingDateDesc(String screeningId, Pageable pageable);
    Page<TheaterBooking> findByStatusOrderByBookingDateDesc(TheaterBooking.BookingStatus status, Pageable pageable);

    List<TheaterBooking> findByScreeningIdAndStatus(String screeningId, TheaterBooking.BookingStatus status);
    List<TheaterBooking> findByScreeningIdAndSeatId(String screeningId, String seatId);

    Optional<TheaterBooking> findByBookingReference(String bookingReference);

    @Query("SELECT COUNT(b) FROM TheaterBooking b WHERE b.status = 'CONFIRMED'")
    Long countConfirmedBookings();

    @Query("SELECT COUNT(b) FROM TheaterBooking b WHERE DATE(b.bookingDate) = CURRENT_DATE AND b.status = 'CONFIRMED'")
    Long countTodayBookings();

    @Query("SELECT SUM(b.pricePaid) FROM TheaterBooking b WHERE b.status = 'CONFIRMED'")
    java.math.BigDecimal calculateTotalRevenue();

    @Query("SELECT SUM(b.pricePaid) FROM TheaterBooking b WHERE DATE(b.bookingDate) = CURRENT_DATE AND b.status = 'CONFIRMED'")
    java.math.BigDecimal calculateTodayRevenue();

    @Query("SELECT COUNT(b) FROM TheaterBooking b WHERE b.screening.id = :screeningId AND b.seat.id = :seatId AND b.status = 'CONFIRMED'")
    Long countConfirmedBookingsByScreeningAndSeat(@Param("screeningId") String screeningId, @Param("seatId") String seatId);

    boolean existsByScreeningIdAndSeatIdAndStatus(String screeningId, String seatId, TheaterBooking.BookingStatus status);
}