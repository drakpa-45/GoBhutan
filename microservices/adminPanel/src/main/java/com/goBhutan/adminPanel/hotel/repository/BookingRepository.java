package com.goBhutan.adminPanel.hotel.repository;


import com.goBhutan.adminPanel.hotel.entity.Booking;
import com.goBhutan.adminPanel.hotel.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByBookingReference(String bookingReference);

    List<Booking> findByHotelId(Long hotelId);


    // Check if the room is already booked for the given date range
    @Query("""
        SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END
        FROM Booking b
        WHERE b.room = :room
          AND b.status IN ('CONFIRMED', 'CHECKED_IN')
          AND (
                (:checkInDate BETWEEN b.checkInDate AND b.checkOutDate)
             OR (:checkOutDate BETWEEN b.checkInDate AND b.checkOutDate)
             OR (b.checkInDate BETWEEN :checkInDate AND :checkOutDate)
          )
    """)
    boolean existsByRoomAndDateRange(
            @Param("room") Room room,
            @Param("checkInDate") java.time.LocalDate checkInDate,
            @Param("checkOutDate") java.time.LocalDate checkOutDate
    );
    @Query("SELECT b.id AS id,g.cid AS cid, g.name AS guestName, r.roomNumber AS roomNumber, b.status AS status, b.bookingReference AS bookingReference " +
            "FROM Booking b JOIN b.room r LEFT JOIN b.guests g WHERE b.hotel.id = :hotelId")
    List<BookingSummary> findBookingSummariesByHotelId(Long hotelId);

    @Query("SELECT COUNT(b) FROM Booking b " +
            "LEFT JOIN b.hotel h " +
            "WHERE h.adminUserId = :userId " +
            "AND b.status IN :statuses")
    Long countByUserIdAndStatuses(@Param("userId") String userId,
                                  @Param("statuses") List<String> statuses);
}

