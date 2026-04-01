package com.goBhutan.adminPanel.theater.repository;

import com.goBhutan.adminPanel.theater.entity.TheaterBooking;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TheaterBookingRepository extends JpaRepository<TheaterBooking, Long> {

    Optional<TheaterBooking> findByBookingRef(String bookingRef);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM TheaterBooking b WHERE b.bookingRef = :bookingRef")
    Optional<TheaterBooking> findByBookingRefForUpdate(@Param("bookingRef") String bookingRef);
}