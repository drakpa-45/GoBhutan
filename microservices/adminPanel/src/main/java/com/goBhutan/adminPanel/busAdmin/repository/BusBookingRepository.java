package com.goBhutan.adminPanel.busAdmin.repository;

import com.goBhutan.adminPanel.busAdmin.entity.Bookings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BusBookingRepository extends JpaRepository<Bookings, Long> {
    // schedule_id foreign key
    List<Bookings> findBySchedule_Id(Long scheduleId);

    // by owner/adminUserId through b.schedule.bus.adminUserId
    List<Bookings> findBySchedule_Bus_AdminUserId(String adminUserId);

    // date range + owner
    List<Bookings> findBySchedule_Bus_AdminUserIdAndBookingTimeBetween(
            String adminUserId, LocalDateTime startDate, LocalDateTime endDate
    );

    // sums still need @Query (Spring Data can't derive SUM)
    @Query("""
           SELECT COALESCE(SUM(b.totalAmount), 0)
           FROM Bookings b
           WHERE b.schedule.bus.adminUserId = :adminUserId
             AND b.status = :status
           """)
    BigDecimal getTotalRevenueByAdminUserIdAndStatus(@Param("adminUserId") String adminUserId,
                                                     @Param("status") Bookings.BookingStatus status);

    @Query("""
           SELECT COALESCE(SUM(b.totalAmount), 0)
           FROM Bookings b
           WHERE b.schedule.bus.adminUserId = :adminUserId
             AND b.status = :status
             AND b.bookingTime BETWEEN :startDate AND :endDate
           """)
    BigDecimal getRevenueByAdminUserIdAndDateRangeAndStatus(@Param("adminUserId") String adminUserId,
                                                            @Param("startDate") LocalDateTime startDate,
                                                            @Param("endDate") LocalDateTime endDate,
                                                            @Param("status") Bookings.BookingStatus status);
}
