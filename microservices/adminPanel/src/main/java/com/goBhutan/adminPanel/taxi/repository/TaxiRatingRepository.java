package com.goBhutan.adminPanel.taxi.repository;

import com.goBhutan.adminPanel.taxi.entity.TaxiRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TaxiRatingRepository extends JpaRepository<TaxiRating, Long> {

    Optional<TaxiRating> findByBookingId(Long bookingId);

    List<TaxiRating> findByDriverIdOrderByCreatedAtDesc(String driverId);

    boolean existsByBookingId(Long bookingId);

    /** Average rating for a driver */
    @Query("SELECT AVG(r.rating) FROM TaxiRating r WHERE r.driverId = :driverId")
    Double findAverageRatingByDriverId(@Param("driverId") String driverId);

    /** Total rating count for a driver */
    long countByDriverId(String driverId);
}