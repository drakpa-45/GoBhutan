package com.goBhutan.adminPanel.busAdmin.service;

import com.goBhutan.adminPanel.busAdmin.dto.ScheduleRequest;
import com.goBhutan.adminPanel.busAdmin.entity.Bus;
import com.goBhutan.adminPanel.busAdmin.entity.BusRouteMap;
import com.goBhutan.adminPanel.busAdmin.entity.Route;
import com.goBhutan.adminPanel.busAdmin.entity.Schedule;
import com.goBhutan.adminPanel.busAdmin.enums.RecurrenceType;
import com.goBhutan.adminPanel.busAdmin.repository.BusRepository;
import com.goBhutan.adminPanel.busAdmin.repository.BusRouteMapRepository;
import com.goBhutan.adminPanel.busAdmin.repository.BusRouteRepository;
import com.goBhutan.adminPanel.busAdmin.repository.BusScheduleRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class BusScheduleService {
    @Autowired
    private BusScheduleRepository scheduleRepository;

    @Autowired
    private BusRepository busRepository;

    @Autowired
    private BusRouteRepository routeRepository;

    @Autowired
    private BusRouteMapRepository busRouteMapRepository;

    private static final Logger log = LoggerFactory.getLogger(BusScheduleService.class);

    public Schedule createSchedule(ScheduleRequest request, String adminUserId) {
        validateTimesAndPrice(request);

        Bus bus = loadBus(request.getBusId(), adminUserId);
        Route route = loadRoute(request.getRouteId(), adminUserId);
        ensureRouteBelongsToBus(route, bus);

        Schedule schedule = new Schedule();
        schedule.setDepartureTime(request.getDepartureTime());
        schedule.setArrivalTime(request.getArrivalTime());
        schedule.setPrice(request.getPrice());
        schedule.setBus(bus);
        schedule.setRoute(route);
        schedule.setAvailableSeats(bus.getTotalSeats());
        schedule.setActive(Boolean.TRUE);

        return scheduleRepository.save(schedule);
    }

    public Schedule updateSchedule(Long scheduleId, ScheduleRequest request, String adminUserId) {
        validateTimesAndPrice(request);

        Schedule schedule = getScheduleById(scheduleId, adminUserId);

        Bus bus = loadBus(request.getBusId(), adminUserId);
        Route route = loadRoute(request.getRouteId(), adminUserId);
        ensureRouteBelongsToBus(route, bus);

        schedule.setDepartureTime(request.getDepartureTime());
        schedule.setArrivalTime(request.getArrivalTime());
        schedule.setPrice(request.getPrice());
        schedule.setBus(bus);
        schedule.setRoute(route);

        return scheduleRepository.save(schedule);
    }

    public Schedule toggleScheduleStatus(Long scheduleId, String adminUserId) {
        Schedule schedule = getScheduleById(scheduleId, adminUserId);
        schedule.setActive(Boolean.TRUE.equals(schedule.getActive()) ? Boolean.FALSE : Boolean.TRUE);
        return scheduleRepository.save(schedule);
    }

    public void deleteSchedule(Long scheduleId, String adminUserId) {
        Schedule schedule = getScheduleById(scheduleId, adminUserId);
        scheduleRepository.delete(schedule);
    }

    // -------------------- Queries --------------------

    public List<Schedule> getSchedulesByOwner(String adminUserId) {
        // Prefer property paths instead of custom method names when possible
        return scheduleRepository.findByBus_AdminUserId(adminUserId);
    }

   /* public List<Schedule> getSchedulesByBus(Long busId, String adminUserId) {
        // Avoid leaking other owners’ data
        return scheduleRepository.findByBus_IdAndBus_AdminUserId(busId, adminUserId);
    }*/

    public List<Schedule> getSchedulesByRoute(Long routeId, String adminUserId) {
        return scheduleRepository.findByRoute_IdAndBus_AdminUserId(routeId, adminUserId);
    }

    public Schedule getScheduleById(Long scheduleId, String adminUserId) {
        return scheduleRepository.findByIdAndBus_AdminUserId(scheduleId, adminUserId)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));
    }

    public List<Schedule> getSchedulesByDateRange(String adminUserId, LocalDateTime startDate, LocalDateTime endDate) {
        // Name your repo method to match entity fields:
        // e.g., findByBus_AdminUserIdAndDepartureTimeBetween(...)
        return scheduleRepository.findByBus_AdminUserIdAndDepartureTimeBetween(adminUserId, startDate, endDate);
    }

    // -------------------- Helpers --------------------

    private Bus loadBus(Long busId, String adminUserId) {
        return busRepository.findByIdAndAdminUserId(busId, adminUserId)
                .orElseThrow(() -> new RuntimeException("Bus not found"));
    }

    private Route loadRoute(Long routeId, String adminUserId) {
        // Route guarded by adminUser via the route->bus relationship
        return routeRepository.findByIdAndBus_AdminUserId(routeId, adminUserId)
                .orElseThrow(() -> new RuntimeException("Route not found"));
    }

    private void ensureRouteBelongsToBus(Route route, Bus bus) {
        if (!route.getBus().getId().equals(bus.getId())) {
            throw new RuntimeException("Route does not belong to the specified bus");
        }
    }

    private void validateTimesAndPrice(ScheduleRequest request) {
        if (request.getDepartureTime() == null || request.getArrivalTime() == null) {
            throw new RuntimeException("Departure and arrival times are required");
        }
        if (!request.getArrivalTime().isAfter(request.getDepartureTime())) {
            throw new RuntimeException("Arrival time must be after departure time");
        }
        if (request.getPrice() == null || request.getPrice().signum() < 0) {
            throw new RuntimeException("Price must be zero or positive");
        }
    }


    public List<Schedule> generateSchedules(Long busId, LocalDate startDate, int daysToGenerate, String adminUserId) {
        Bus bus = busRepository.findByIdAndAdminUserId(busId, adminUserId)
                .orElseThrow(() -> new RuntimeException("Bus not found"));

        List<BusRouteMap> routes = busRouteMapRepository.findByBusAndActiveTrue(bus);
        if (routes.isEmpty()) throw new RuntimeException("No active route mappings for this bus.");

        if (startDate == null) startDate = LocalDate.now();
        if (daysToGenerate <= 0) daysToGenerate = 14;

        List<Schedule> newSchedules = new ArrayList<>();

        for (int i = 0; i < daysToGenerate; i++) {
            LocalDate currentDate = startDate.plusDays(i);

            if (!shouldRunOnDay(bus.getRecurrenceType(), bus.getOperatingDays(), currentDate))
                continue;

            for (BusRouteMap map : routes) {
                LocalDateTime departure = currentDate.atTime(map.getDepartureTime());
                int duration = Optional.ofNullable(map.getEstimatedDuration())
                        .orElse(Optional.ofNullable(map.getRoute().getEstimatedDuration()).orElse(120));
                LocalDateTime arrival = departure.plusMinutes(duration);

                boolean exists = scheduleRepository.existsByBusAndRouteAndDepartureTimeBetween(
                        bus, map.getRoute(), departure.minusMinutes(1), departure.plusMinutes(1)
                );
                if (exists) continue;

                Schedule schedule = new Schedule();
                schedule.setBus(bus);
                schedule.setRoute(map.getRoute());
                schedule.setDepartureTime(departure);
                schedule.setArrivalTime(arrival);
                schedule.setAvailableSeats(bus.getTotalSeats());
                schedule.setPrice(Optional.ofNullable(map.getCustomFare()).orElse(map.getRoute().getBaseFare()));
                schedule.setActive(true);

                newSchedules.add(schedule);
            }
        }

        List<Schedule> saved = scheduleRepository.saveAll(newSchedules);
        log.info("✅ Generated {} schedules for bus {} ({} routes)", saved.size(), bus.getBusNumber(), routes.size());
        return saved;
    }

    private boolean shouldRunOnDay(RecurrenceType recurrence, Set<DayOfWeek> customDays, LocalDate date) {
        switch (recurrence) {
            case DAILY: return true;
            case ALTERNATE: return date.getDayOfYear() % 2 == 0;
            case WEEKDAYS: return !isWeekend(date);
            case WEEKENDS: return isWeekend(date);
            case CUSTOM: return customDays != null && customDays.contains(date.getDayOfWeek());
            default: return false;
        }
    }

    private boolean isWeekend(LocalDate date) {
        return date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
    }

    // Extra fetch methods
    public List<Schedule> getSchedulesByBus(Long busId, String adminUserId) {
        return scheduleRepository.findByBus_IdAndBus_AdminUserId(busId, adminUserId);
    }
}