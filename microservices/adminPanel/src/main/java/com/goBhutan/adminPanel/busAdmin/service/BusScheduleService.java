package com.goBhutan.adminPanel.busAdmin.service;

import com.goBhutan.adminPanel.busAdmin.dto.AppScheduleResponse;
import com.goBhutan.adminPanel.busAdmin.entity.Bus;
import com.goBhutan.adminPanel.busAdmin.entity.BusRoute;
import com.goBhutan.adminPanel.busAdmin.entity.Schedule;
import com.goBhutan.adminPanel.busAdmin.enums.BookingStatus;
import com.goBhutan.adminPanel.busAdmin.enums.RecurrenceType;
import com.goBhutan.adminPanel.busAdmin.repository.BusRepository;
import com.goBhutan.adminPanel.busAdmin.repository.BusRouteRepository;
import com.goBhutan.adminPanel.busAdmin.repository.BusScheduleRepository;
import com.goBhutan.adminPanel.busAdmin.repository.SeatBookingRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
@Service
@Transactional
@RequiredArgsConstructor
public class BusScheduleService {

    private static final Logger log = LoggerFactory.getLogger(BusScheduleService.class);
    private static final int MAX_GENERATION_DAYS = 30;
    private static final int MAX_INCREMENTAL_LOOKAHEAD_DAYS = 370;
    private static final int CHECK_IN_OFFSET_MINUTES = 30;
    private static final DateTimeFormatter TIME_DISPLAY_FORMATTER =
            DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);

    private final BusScheduleRepository scheduleRepository;
    private final BusRepository busRepository;
    private final BusRouteRepository busRouteRepository;
    private final SeatBookingRepository seatBookingRepository;

    // ========================== AUTO GENERATION ONLY ==========================
    public List<Schedule> generateSchedules(Long busId, LocalDate startDate, int days, String adminUserId) {

        // Validate bus ownership
        Bus bus = busRepository.lockByIdAndAdminUserId(busId, adminUserId)
                .orElseThrow(() -> new RuntimeException("Bus not found"));

        // Load all active routes
        List<BusRoute> routes = busRouteRepository
                .findByBus_IdAndBus_AdminUserIdAndActiveTrue(busId, adminUserId);

        if (routes.isEmpty()) {
            throw new RuntimeException("No active routes for this bus.");
        }

        startDate = normalizeStartDate(startDate);
        days = normalizeDays(days);

        Map<Long, Bus> buses = new HashMap<>();
        buses.put(bus.getId(), bus);
        Set<ScheduleKey> expectedSchedules = new HashSet<>();
        List<Schedule> result = upsertSchedulesForRoutes(routes, buses, startDate, days, expectedSchedules);

        deactivateStaleSchedules(bus, adminUserId, startDate, days, expectedSchedules);
        return scheduleRepository.saveAll(result);
    }

    public List<Schedule> generateNextSchedulesForAllActiveBuses() {
        LocalDateTime now = LocalDateTime.now();
        List<Bus> activeBuses = busRepository.findActiveBuses();
        List<Schedule> schedulesToSave = new ArrayList<>();

        for (Bus activeBus : activeBuses) {
            if (activeBus.getId() == null) {
                continue;
            }

            Bus bus = busRepository.lockActiveById(activeBus.getId()).orElse(null);
            if (bus == null) {
                continue;
            }

            List<BusRoute> routes = busRouteRepository.findActiveRoutesByBusId(bus.getId());
            if (routes.isEmpty()) {
                continue;
            }

            ensureScheduleAnchors(bus, routes);
            for (BusRoute route : routes) {
                try {
                    Schedule schedule = buildNextIncrementalSchedule(bus, route, now);
                    if (schedule != null) {
                        schedulesToSave.add(schedule);
                    }
                } catch (RuntimeException ex) {
                    log.warn(
                            "booking-schedule skip busId={} routeId={} reason={}",
                            bus.getId(),
                            route.getId(),
                            ex.getMessage());
                }
            }
        }

        if (schedulesToSave.isEmpty()) {
            return List.of();
        }
        return scheduleRepository.saveAll(schedulesToSave);
    }

    /* unimplemented
     *
     * Keep this disabled until the API needs route-selected generation.
     * Current implemented generation is bus-based: one busId generates
     * schedules for all active routes mapped to that bus.
     *
     * public List<Schedule> generateSchedulesForRoutes(Set<Long> routeIds,
     *                                                  LocalDate startDate,
     *                                                  int days,
     *                                                  String adminUserId) {
     *     Set<Long> selectedRouteIds = normalizeRouteIds(routeIds);
     *     List<BusRoute> routes = busRouteRepository.findActiveRoutesByIdsAndAdminUserId(selectedRouteIds, adminUserId);
     *     if (routes.size() != selectedRouteIds.size()) {
     *         throw new RuntimeException("One or more routes were not found or are inactive.");
     *     }
     *
     *     startDate = normalizeStartDate(startDate);
     *     days = normalizeDays(days);
     *
     *     Map<Long, Bus> buses = lockBusesForRoutes(routes, adminUserId);
     *     Set<ScheduleKey> expectedSchedules = new HashSet<>();
     *     List<Schedule> result = upsertSchedulesForRoutes(routes, buses, startDate, days, expectedSchedules);
     *
     *     deactivateStaleSchedulesForRoutes(selectedRouteIds, adminUserId, startDate, days, expectedSchedules);
     *     return scheduleRepository.saveAll(result);
     * }
     */

    private List<Schedule> upsertSchedulesForRoutes(List<BusRoute> routes,
                                                    Map<Long, Bus> buses,
                                                    LocalDate startDate,
                                                    int days,
                                                    Set<ScheduleKey> expectedSchedules) {
        ensureScheduleAnchors(routes, buses);

        LocalDateTime now = LocalDateTime.now();
        List<Schedule> result = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);

            for (BusRoute route : routes) {
                Bus bus = buses.get(route.getBus().getId());
                if (!shouldRun(bus, date))
                    continue;

                LocalDateTime departure = LocalDateTime.of(date, route.getDepartureTime());
                if (!departure.isAfter(now)) {
                    continue;
                }

                LocalDateTime arrival = departure.plusMinutes(getEstimatedDurationMinutes(route));
                expectedSchedules.add(new ScheduleKey(route.getId(), departure));

                Schedule existing = scheduleRepository.findByBusAndRouteAndDepartureTime(bus, route, departure)
                        .orElse(null);

                if (existing != null) {
                    if (refreshSchedule(existing, route, departure, arrival, bus)) {
                        result.add(existing);
                    }
                    continue;
                }

                Schedule schedule = new Schedule();
                schedule.setBus(bus);
                schedule.setRoute(route);
                schedule.setDepartureTime(departure);
                schedule.setArrivalTime(arrival);
                schedule.setAvailableSeats(bus.getTotalSeats());
                applyFareSnapshot(schedule, route);
                schedule.setActive(true);

                result.add(schedule);
            }
        }

        return result;
    }

    private Schedule buildNextIncrementalSchedule(Bus bus, BusRoute route, LocalDateTime now) {
        Schedule latestSchedule = scheduleRepository
                .findTopByBusAndRouteAndActiveTrueOrderByDepartureTimeDesc(bus, route)
                .orElse(null);

        LocalDate searchDate = getNextIncrementalSearchDate(latestSchedule, now);
        LocalDate nextOperatingDate = findNextOperatingDate(bus, searchDate);
        LocalDateTime departure = LocalDateTime.of(nextOperatingDate, route.getDepartureTime());
        if (!departure.isAfter(now)) {
            return null;
        }

        LocalDateTime arrival = departure.plusMinutes(getEstimatedDurationMinutes(route));
        Schedule existing = scheduleRepository.findByBusAndRouteAndDepartureTime(bus, route, departure)
                .orElse(null);
        if (existing != null) {
            return refreshSchedule(existing, route, departure, arrival, bus) ? existing : null;
        }

        Schedule schedule = new Schedule();
        schedule.setBus(bus);
        schedule.setRoute(route);
        schedule.setDepartureTime(departure);
        schedule.setArrivalTime(arrival);
        schedule.setAvailableSeats(bus.getTotalSeats());
        applyFareSnapshot(schedule, route);
        schedule.setActive(true);
        return schedule;
    }

    private LocalDate getNextIncrementalSearchDate(Schedule latestSchedule, LocalDateTime now) {
        LocalDate earliestDate = now.toLocalDate().plusDays(1);
        if (latestSchedule == null || latestSchedule.getDepartureTime() == null) {
            return earliestDate;
        }

        LocalDate dateAfterLatestSchedule = latestSchedule.getDepartureTime().toLocalDate().plusDays(1);
        return dateAfterLatestSchedule.isAfter(earliestDate) ? dateAfterLatestSchedule : earliestDate;
    }

    private LocalDate findNextOperatingDate(Bus bus, LocalDate startDate) {
        LocalDate date = startDate;
        for (int i = 0; i < MAX_INCREMENTAL_LOOKAHEAD_DAYS; i++) {
            if (shouldRun(bus, date)) {
                return date;
            }
            date = date.plusDays(1);
        }
        throw new RuntimeException("No valid operating date found within " + MAX_INCREMENTAL_LOOKAHEAD_DAYS + " days");
    }

    private LocalDate normalizeStartDate(LocalDate startDate) {
        LocalDate normalizedStartDate = startDate != null ? startDate : LocalDate.now();
        if (normalizedStartDate.isBefore(LocalDate.now())) {
            throw new RuntimeException("Cannot generate schedules for past dates");
        }
        return normalizedStartDate;
    }

    private int normalizeDays(int days) {
        if (days <= 0) {
            return 1;
        }
        if (days > MAX_GENERATION_DAYS) {
            throw new RuntimeException("Cannot generate more than " + MAX_GENERATION_DAYS + " days at a time");
        }
        return days;
    }

    private boolean refreshSchedule(Schedule schedule,
                                    BusRoute route,
                                    LocalDateTime departure,
                                    LocalDateTime arrival,
                                    Bus bus) {
        boolean changed = false;

        if (!Objects.equals(schedule.getDepartureTime(), departure)) {
            schedule.setDepartureTime(departure);
            changed = true;
        }

        if (!Objects.equals(schedule.getArrivalTime(), arrival)) {
            schedule.setArrivalTime(arrival);
            changed = true;
        }

        int availableSeats = calculateAvailableSeats(bus, schedule);
        if (!Objects.equals(schedule.getAvailableSeats(), availableSeats)) {
            schedule.setAvailableSeats(availableSeats);
            changed = true;
        }

        if (!Boolean.TRUE.equals(schedule.getActive())) {
            schedule.setActive(true);
            changed = true;
        }

        if (!schedule.hasFareSnapshot()) {
            applyFareSnapshot(schedule, route);
            changed = true;
        }

        return changed;
    }

    private boolean shouldRun(Bus bus, LocalDate date) {
        RecurrenceType recurrenceType = bus.getRecurrenceType();

        if (recurrenceType == null) {
            return true;
        }

        switch (recurrenceType) {
            case DAILY: return true;
            case WEEKDAYS: return !isWeekend(date);
            case WEEKENDS: return isWeekend(date);
            case CUSTOM:
                Set<DayOfWeek> operatingDays = bus.getOperatingDays();
                if (operatingDays == null || operatingDays.isEmpty()) {
                    throw new RuntimeException("Operating days are required for CUSTOM recurrence");
                }
                return operatingDays.contains(date.getDayOfWeek());
            case ALTERNATE:
                LocalDate anchorDate = bus.getScheduleAnchorDate();
                return !date.isBefore(anchorDate) && ChronoUnit.DAYS.between(anchorDate, date) % 2 == 0;
            default: return false;
        }
    }

    private void ensureScheduleAnchors(List<BusRoute> routes, Map<Long, Bus> buses) {
        Map<Long, List<BusRoute>> routesByBus = new HashMap<>();
        for (BusRoute route : routes) {
            Long busId = route.getBus().getId();
            if (!buses.containsKey(busId)) {
                throw new RuntimeException("Bus not found");
            }
            routesByBus.computeIfAbsent(busId, ignored -> new ArrayList<>()).add(route);
        }

        for (Map.Entry<Long, List<BusRoute>> entry : routesByBus.entrySet()) {
            ensureScheduleAnchors(buses.get(entry.getKey()), entry.getValue());
        }
    }

    private void ensureScheduleAnchors(Bus bus, List<BusRoute> routes) {
        if (bus.getScheduleAnchorDate() == null) {
            LocalDate fallbackAnchorDate = routes.stream()
                    .map(BusRoute::getCreatedAt)
                    .filter(Objects::nonNull)
                    .map(LocalDateTime::toLocalDate)
                    .min(LocalDate::compareTo)
                    .orElse(LocalDate.now());
            bus.setScheduleAnchorDate(fallbackAnchorDate);
        }

    }

    private int calculateAvailableSeats(Bus bus, Schedule schedule) {
        if (schedule.getId() == null) {
            return bus.getTotalSeats();
        }

        long bookedSeats = seatBookingRepository.countByScheduleIdAndStatus(schedule.getId(), BookingStatus.BOOKED);
        return Math.max(bus.getTotalSeats() - Math.toIntExact(bookedSeats), 0);
    }

    private void deactivateStaleSchedules(Bus bus,
                                          String adminUserId,
                                          LocalDate startDate,
                                          int days,
                                          Set<ScheduleKey> expectedSchedules) {
        LocalDateTime rangeStart = startDate.atStartOfDay();
        LocalDateTime rangeEnd = startDate.plusDays(days - 1L).atTime(LocalTime.MAX);

        List<Schedule> activeSchedules = scheduleRepository
                .findByBus_IdAndBus_AdminUserIdAndActiveTrueAndDepartureTimeBetween(
                        bus.getId(), adminUserId, rangeStart, rangeEnd);

        deactivateUnexpectedSchedules(activeSchedules, expectedSchedules);
    }

    private void deactivateUnexpectedSchedules(List<Schedule> activeSchedules,
                                               Set<ScheduleKey> expectedSchedules) {
        List<Schedule> schedulesToDeactivate = new ArrayList<>();
        for (Schedule schedule : activeSchedules) {
            Long routeId = schedule.getRoute() != null ? schedule.getRoute().getId() : null;
            ScheduleKey scheduleKey = new ScheduleKey(routeId, schedule.getDepartureTime());

            if (!expectedSchedules.contains(scheduleKey) && !hasBookedSeats(schedule.getId())) {
                schedule.setActive(false);
                schedulesToDeactivate.add(schedule);
            }
        }

        if (!schedulesToDeactivate.isEmpty()) {
            scheduleRepository.saveAll(schedulesToDeactivate);
        }
    }

    private boolean hasBookedSeats(Long scheduleId) {
        return seatBookingRepository.existsByScheduleIdAndStatus(scheduleId, BookingStatus.BOOKED);
    }

    private boolean isWeekend(LocalDate date) {
        return date.getDayOfWeek() == DayOfWeek.SATURDAY ||
                date.getDayOfWeek() == DayOfWeek.SUNDAY;
    }

    private record ScheduleKey(Long routeId, LocalDateTime departureTime) {
    }

    // ========================== READ-ONLY QUERIES ==========================
    public List<Schedule> getSchedulesByBus(Long busId, String adminUserId) {
        return getSchedulesByBus(busId, adminUserId, false);
    }

    public List<Schedule> getSchedulesByBus(Long busId, String adminUserId, boolean includeInactive) {
        if (includeInactive) {
            return scheduleRepository.findByBus_IdAndBus_AdminUserId(busId, adminUserId);
        }
        return scheduleRepository.findByBus_IdAndBus_AdminUserIdAndActiveTrue(busId, adminUserId);
    }

    public List<Schedule> getSchedulesByRoute(Long routeId, String adminUserId) {
        return getSchedulesByRoute(routeId, adminUserId, false);
    }

    public List<Schedule> getSchedulesByRoute(Long routeId, String adminUserId, boolean includeInactive) {
        if (includeInactive) {
            return scheduleRepository.findByRoute_IdAndBus_AdminUserId(routeId, adminUserId);
        }
        return scheduleRepository.findByRoute_IdAndBus_AdminUserIdAndActiveTrue(routeId, adminUserId);
    }

    public Schedule getScheduleById(Long id, String adminUserId) {
        return scheduleRepository.findByIdAndBus_AdminUserId(id, adminUserId)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));
    }

    public List<Schedule> getSchedulesByDateRange(String adminUserId,
                                                  LocalDateTime start,
                                                  LocalDateTime end) {
        return getSchedulesByDateRange(adminUserId, start, end, false);
    }

    public List<Schedule> getSchedulesByDateRange(String adminUserId,
                                                  LocalDateTime start,
                                                  LocalDateTime end,
                                                  boolean includeInactive) {
        if (includeInactive) {
            return scheduleRepository.findByBus_AdminUserIdAndDepartureTimeBetween(adminUserId, start, end);
        }
        return scheduleRepository.findByBus_AdminUserIdAndActiveTrueAndDepartureTimeBetween(adminUserId, start, end);
    }

    public List<AppScheduleResponse> getAvailableSchedulesForApp(Long routeId, LocalDate travelDate) {
        if (routeId == null) {
            throw new RuntimeException("Route ID is required");
        }
        if (travelDate == null) {
            throw new RuntimeException("Travel date is required");
        }
        if (travelDate.isBefore(LocalDate.now())) {
            throw new RuntimeException("Travel date cannot be in the past");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = travelDate.atStartOfDay();
        LocalDateTime end = travelDate.atTime(LocalTime.MAX);

        return scheduleRepository.findBookableAppSchedulesByRouteAndDate(routeId, start, end, now)
                .stream()
                .map(this::toAppScheduleResponse)
                .toList();
    }

    private AppScheduleResponse toAppScheduleResponse(Schedule schedule) {
        Bus bus = schedule.getBus();
        BusRoute route = schedule.getRoute();
        LocalDateTime checkInTime = calculateCheckInTime(schedule.getDepartureTime());

        return new AppScheduleResponse(
                schedule.getId(),
                route.getId(),
                bus.getId(),
                bus.getBusNumber(),
                bus.getBusName(),
                bus.getBusType(),
                bus.getTotalSeats(),
                bus.getAmenities(),
                route.getSource(),
                route.getDestination(),
                route.getDistance(),
                schedule.getBaseFare(),
                schedule.getAppCharges(),
                schedule.getFinalFare(),
                route.getEstimatedDurationMinutes(),
                schedule.getDepartureTime() != null ? schedule.getDepartureTime().toLocalDate() : null,
                checkInTime,
                formatDisplayTime(checkInTime),
                schedule.getDepartureTime(),
                formatDisplayTime(schedule.getDepartureTime()),
                schedule.getArrivalTime(),
                formatDisplayTime(schedule.getArrivalTime()),
                schedule.getAvailableSeats()
        );
    }

    private LocalDateTime calculateCheckInTime(LocalDateTime departureTime) {
        return departureTime != null ? departureTime.minusMinutes(CHECK_IN_OFFSET_MINUTES) : null;
    }

    private String formatDisplayTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(TIME_DISPLAY_FORMATTER) : null;
    }

    private int getEstimatedDurationMinutes(BusRoute route) {
        Integer estimatedDurationMinutes = route.getEstimatedDurationMinutes();
        if (estimatedDurationMinutes == null || estimatedDurationMinutes <= 0) {
            throw new RuntimeException("Estimated duration in minutes is required for route " + route.getId());
        }
        return estimatedDurationMinutes;
    }

    private void applyFareSnapshot(Schedule schedule, BusRoute route) {
        if (route.getBaseFare() == null) {
            throw new RuntimeException("Base fare is required for route " + route.getId());
        }
        BigDecimal baseFare = money(route.getBaseFare());
        BigDecimal appCharges = money(route.getAppCharges());

        schedule.setBaseFare(baseFare);
        schedule.setAppCharges(appCharges);
        schedule.setFinalFare(baseFare.add(appCharges));
    }

    private BigDecimal money(BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
    }

    // ========================== OPTIONAL ==========================
    public Schedule toggleScheduleStatus(Long id, String adminUserId) {
        Schedule schedule = scheduleRepository.lockByIdAndAdminUserId(id, adminUserId)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));
        boolean newActive = !Boolean.TRUE.equals(schedule.getActive());

        if (!newActive) {
            ensureNoBookedSeats(id);
        }

        schedule.setActive(newActive);
        return scheduleRepository.save(schedule);
    }

    public void deleteSchedule(Long id, String adminUserId) {
        Schedule schedule = scheduleRepository.lockByIdAndAdminUserId(id, adminUserId)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));

        ensureNoBookedSeats(id);
        schedule.setActive(false);
        scheduleRepository.save(schedule);
    }

    private void ensureNoBookedSeats(Long scheduleId) {
        if (seatBookingRepository.existsByScheduleIdAndStatus(scheduleId, BookingStatus.BOOKED)) {
            throw new RuntimeException("Cannot deactivate schedule with booked seats.");
        }
    }
}
