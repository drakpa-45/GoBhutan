package com.goBhutan.adminPanel.taxi.repository;


import com.goBhutan.adminPanel.taxi.entity.TripLocationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TripLocationHistoryRepository extends JpaRepository<TripLocationHistory, Long> {

    /** Full ordered path for a trip — used for replay and distance verification */
    List<TripLocationHistory> findByBookingIdOrderByRecordedAtAsc(Long bookingId);

    /** Last known point — used to compute incremental Haversine distance */
    Optional<TripLocationHistory> findTopByBookingIdOrderByRecordedAtDesc(Long bookingId);

    /** Total distance driven on a trip (last row's cumulative value) */
    @Query("""
        SELECT h.cumulativeDistanceKm FROM TripLocationHistory h
        WHERE h.bookingId = :bookingId
        ORDER BY h.recordedAt DESC
        LIMIT 1
        """)
    Optional<Double> findTotalDistanceByBookingId(@Param("bookingId") Long bookingId);

    int countByBookingId(Long bookingId);
}
