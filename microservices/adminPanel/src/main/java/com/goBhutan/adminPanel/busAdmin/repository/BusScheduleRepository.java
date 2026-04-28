package com.goBhutan.adminPanel.busAdmin.repository;

import com.goBhutan.adminPanel.busAdmin.entity.Bus;
import com.goBhutan.adminPanel.busAdmin.entity.BusRoute;
import com.goBhutan.adminPanel.busAdmin.entity.Schedule;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BusScheduleRepository extends JpaRepository<Schedule, Long> {

    List<Schedule> findByBus_AdminUserId(String adminUserId);
    List<Schedule> findByBus_IdAndBus_AdminUserId(Long busId, String adminUserId);
    List<Schedule> findByBus_IdAndBus_AdminUserIdAndActiveTrue(Long busId, String adminUserId);
    List<Schedule> findByBus_IdAndBus_AdminUserIdAndActiveTrueAndDepartureTimeAfter(Long busId,
                                                                                    String adminUserId,
                                                                                    LocalDateTime departureTime);
    List<Schedule> findByBus_IdAndBus_AdminUserIdAndActiveTrueAndDepartureTimeBetween(Long busId,
                                                                                      String adminUserId,
                                                                                      LocalDateTime start,
                                                                                      LocalDateTime end);
    List<Schedule> findByRoute_IdAndBus_AdminUserId(Long routeId, String adminUserId);
    List<Schedule> findByRoute_IdAndBus_AdminUserIdAndActiveTrue(Long routeId, String adminUserId);
    /* unimplemented
     *
     * Used only by disabled route-selected schedule generation.
     *
     * List<Schedule> findByRoute_IdInAndBus_AdminUserIdAndActiveTrueAndDepartureTimeBetween(
     *         Collection<Long> routeIds,
     *         String adminUserId,
     *         LocalDateTime start,
     *         LocalDateTime end);
     */
    List<Schedule> findByRoute_IdAndBus_AdminUserIdAndActiveTrueAndDepartureTimeAfter(Long routeId,
                                                                                      String adminUserId,
                                                                                      LocalDateTime departureTime);
    Optional<Schedule> findByIdAndBus_AdminUserId(Long scheduleId, String adminUserId);
    List<Schedule> findByBus_AdminUserIdAndDepartureTimeBetween(String adminUserId, LocalDateTime start, LocalDateTime end);
    List<Schedule> findByBus_AdminUserIdAndActiveTrueAndDepartureTimeBetween(String adminUserId,
                                                                             LocalDateTime start,
                                                                             LocalDateTime end);

    @Query("""
        SELECT s
        FROM Schedule s
        JOIN FETCH s.route r
        JOIN FETCH s.bus b
        WHERE r.id = :routeId
          AND s.active = true
          AND r.active = true
          AND (b.isActive = true OR b.isActive IS NULL)
          AND s.departureTime BETWEEN :start AND :end
          AND s.departureTime > :now
        ORDER BY s.departureTime ASC
    """)
    List<Schedule> findBookableAppSchedulesByRouteAndDate(
            @Param("routeId") Long routeId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("now") LocalDateTime now);

    @Query("""
        SELECT s
        FROM Schedule s
        JOIN FETCH s.route r
        JOIN FETCH s.bus b
        WHERE r.active = true
          AND s.active = true
          AND (b.isActive = true OR b.isActive IS NULL)
          AND LOWER(TRIM(r.source)) = LOWER(:source)
          AND LOWER(TRIM(r.destination)) = LOWER(:destination)
          AND s.departureTime > :now
        ORDER BY s.departureTime ASC
    """)
    List<Schedule> findBookableAppSchedulesBySourceAndDestination(
            @Param("source") String source,
            @Param("destination") String destination,
            @Param("now") LocalDateTime now);

    @Query("""
        SELECT s
        FROM Schedule s
        JOIN FETCH s.route r
        JOIN FETCH s.bus b
        WHERE r.active = true
          AND s.active = true
          AND (b.isActive = true OR b.isActive IS NULL)
          AND LOWER(TRIM(r.source)) = LOWER(:source)
          AND LOWER(TRIM(r.destination)) = LOWER(:destination)
          AND s.departureTime BETWEEN :start AND :end
          AND s.departureTime > :now
        ORDER BY s.departureTime ASC
    """)
    List<Schedule> findBookableAppSchedulesBySourceDestinationAndDate(
            @Param("source") String source,
            @Param("destination") String destination,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("now") LocalDateTime now);

    boolean existsByBusAndRouteAndDepartureTime(Bus bus, BusRoute route, LocalDateTime departureTime);
    Optional<Schedule> findByBusAndRouteAndDepartureTime(Bus bus, BusRoute route, LocalDateTime departureTime);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Schedule s WHERE s.id = :id AND s.bus.adminUserId = :adminUserId")
    Optional<Schedule> lockByIdAndAdminUserId(@Param("id") Long id,
                                              @Param("adminUserId") String adminUserId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Schedule s WHERE s.id = :id")
    Schedule lockSchedule(@Param("id") Long id);
}
