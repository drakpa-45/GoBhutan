package com.goBhutan.adminPanel.busAdmin.repository;

import com.goBhutan.adminPanel.busAdmin.entity.Bus;
import com.goBhutan.adminPanel.busAdmin.entity.BusRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BusRouteRepository extends JpaRepository<BusRoute, Long> {
    // FETCH ROUTES for a bus (admin validated)
    @Query("""
        SELECT r
        FROM BusRoute r
        WHERE r.bus.id = :busId
          AND r.bus.adminUserId = :adminUserId
          AND r.active = true
          AND (r.bus.isActive = true OR r.bus.isActive IS NULL)
        ORDER BY r.departureTime ASC
    """)
    List<BusRoute> findByBus_IdAndBus_AdminUserIdAndActiveTrue(
            @Param("busId") Long busId,
            @Param("adminUserId") String adminUserId);

    @Query("""
        SELECT r
        FROM BusRoute r
        JOIN FETCH r.bus b
        WHERE b.id = :busId
          AND r.active = true
          AND (b.isActive = true OR b.isActive IS NULL)
        ORDER BY r.departureTime ASC
    """)
    List<BusRoute> findActiveRoutesByBusId(@Param("busId") Long busId);

    @Query("""
        SELECT r
        FROM BusRoute r
        JOIN FETCH r.bus b
        WHERE r.active = true
          AND (b.isActive = true OR b.isActive IS NULL)
          AND LOWER(TRIM(r.source)) = LOWER(:source)
          AND LOWER(TRIM(r.destination)) = LOWER(:destination)
        ORDER BY r.departureTime ASC
    """)
    List<BusRoute> findActiveRoutesBySourceAndDestinationOrderByTime(
            @Param("source") String source,
            @Param("destination") String destination);

    @Query("""
        SELECT DISTINCT r
        FROM Schedule s
        JOIN s.route r
        JOIN FETCH r.bus b
        WHERE r.active = true
          AND s.active = true
          AND (b.isActive = true OR b.isActive IS NULL)
          AND LOWER(TRIM(r.source)) = LOWER(:source)
          AND LOWER(TRIM(r.destination)) = LOWER(:destination)
          AND s.departureTime > :now
        ORDER BY r.departureTime ASC
    """)
    List<BusRoute> findActiveRoutesWithFutureSchedulesBySourceAndDestination(
            @Param("source") String source,
            @Param("destination") String destination,
            @Param("now") LocalDateTime now);

    @Query("""
        SELECT DISTINCT r
        FROM Schedule s
        JOIN s.route r
        JOIN FETCH r.bus b
        WHERE r.active = true
          AND s.active = true
          AND (b.isActive = true OR b.isActive IS NULL)
          AND LOWER(TRIM(r.source)) = LOWER(:source)
          AND LOWER(TRIM(r.destination)) = LOWER(:destination)
          AND s.departureTime BETWEEN :start AND :end
          AND s.departureTime > :now
        ORDER BY r.departureTime ASC
    """)
    List<BusRoute> findActiveRoutesWithBookableSchedulesBySourceDestinationAndDate(
            @Param("source") String source,
            @Param("destination") String destination,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("now") LocalDateTime now);

    /* unimplemented
     *
     * Used only by disabled route-selected schedule generation.
     *
     * @Query("""
     *     SELECT r
     *     FROM BusRoute r
     *     JOIN FETCH r.bus b
     *     WHERE r.id IN :routeIds
     *       AND b.adminUserId = :adminUserId
     *       AND r.active = true
     *       AND (b.isActive = true OR b.isActive IS NULL)
     *     ORDER BY b.id ASC, r.departureTime ASC
     * """)
     * List<BusRoute> findActiveRoutesByIdsAndAdminUserId(
     *         @Param("routeIds") Collection<Long> routeIds,
     *         @Param("adminUserId") String adminUserId);
     */

    // GET ONE ROUTE (admin validated)
    Optional<BusRoute> findByIdAndBus_AdminUserId(Long id, String adminUserId);

    // CHECK DUPLICATE for same time + same source + same destination
    @Query("""
        SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END
        FROM BusRoute r
        WHERE r.bus = :bus
          AND r.active = true
          AND r.departureTime = :departureTime
          AND LOWER(TRIM(r.source)) = LOWER(:source)
          AND LOWER(TRIM(r.destination)) = LOWER(:destination)
    """)
    boolean existsByBusAndDepartureTimeAndSourceAndDestinationAndActiveTrue(
            @Param("bus") Bus bus,
            @Param("departureTime") LocalTime departureTime,
            @Param("source") String source,
            @Param("destination") String destination
    );

    @Query("""
        SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END
        FROM BusRoute r
        WHERE r.bus = :bus
          AND r.active = true
          AND r.id <> :id
          AND r.departureTime = :departureTime
          AND LOWER(TRIM(r.source)) = LOWER(:source)
          AND LOWER(TRIM(r.destination)) = LOWER(:destination)
    """)
    boolean existsByBusAndDepartureTimeAndSourceAndDestinationAndActiveTrueAndIdNot(
            @Param("bus") Bus bus,
            @Param("departureTime") LocalTime departureTime,
            @Param("source") String source,
            @Param("destination") String destination,
            @Param("id") Long id
    );
}
