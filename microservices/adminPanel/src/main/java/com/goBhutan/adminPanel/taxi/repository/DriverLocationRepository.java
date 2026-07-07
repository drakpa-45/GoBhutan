package com.goBhutan.adminPanel.taxi.repository;

import com.goBhutan.adminPanel.taxi.entity.DriverLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DriverLocationRepository extends JpaRepository<DriverLocation, Long> {

    Optional<DriverLocation> findByDriverId(String driverId);

    /**
     * Find online drivers within a bounding box (fast pre-filter before Haversine).
     * Drivers not pinged in the last 2 minutes are excluded (considered offline).
     *
     * latMin/latMax/lngMin/lngMax define a square around the passenger.
     * A 10 km radius at Bhutan's latitude ≈ ±0.09 degrees lat, ±0.12 degrees lng.
     */
    @Query("""
        SELECT d FROM DriverLocation d
        WHERE d.isOnline = true
          AND d.currentBookingId IS NULL
          AND d.lastUpdatedAt >= :cutoff
          AND d.latitude  BETWEEN :latMin AND :latMax
          AND d.longitude BETWEEN :lngMin AND :lngMax
        """)
    List<DriverLocation> findAvailableInBoundingBox(
            @Param("latMin")  BigDecimal latMin,
            @Param("latMax")  BigDecimal latMax,
            @Param("lngMin")  BigDecimal lngMin,
            @Param("lngMax")  BigDecimal lngMax,
            @Param("cutoff")  LocalDateTime cutoff
    );

    /**
     * Same but filtered to a specific dzongkhag (intra-dzongkhag Pull mode).
     */
    @Query("""
        SELECT d FROM DriverLocation d
        WHERE d.isOnline = true
          AND d.currentBookingId IS NULL
          AND d.lastUpdatedAt >= :cutoff
          AND d.currentDzongkhag = :dzongkhag
          AND d.latitude  BETWEEN :latMin AND :latMax
          AND d.longitude BETWEEN :lngMin AND :lngMax
        """)
    List<DriverLocation> findAvailableInDzongkhag(
            @Param("dzongkhag") String dzongkhag,
            @Param("latMin")    BigDecimal latMin,
            @Param("latMax")    BigDecimal latMax,
            @Param("lngMin")    BigDecimal lngMin,
            @Param("lngMax")    BigDecimal lngMax,
            @Param("cutoff")    LocalDateTime cutoff
    );

    List<DriverLocation> findByCurrentBookingId(Long bookingId);
}
