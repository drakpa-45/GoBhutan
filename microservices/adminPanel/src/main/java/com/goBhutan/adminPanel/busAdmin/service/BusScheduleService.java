package com.goBhutan.adminPanel.busAdmin.service;

import com.goBhutan.adminPanel.busAdmin.entity.Bus;
import com.goBhutan.adminPanel.busAdmin.entity.BusRoute;
import com.goBhutan.adminPanel.busAdmin.entity.Schedule;
import com.goBhutan.adminPanel.busAdmin.repository.BusRepository;
import com.goBhutan.adminPanel.busAdmin.repository.BusRouteRepository;
import com.goBhutan.adminPanel.busAdmin.repository.BusScheduleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Service
@Transactional
@RequiredArgsConstructor
public class BusScheduleService {

    private final BusScheduleRepository scheduleRepository;
    private final BusRepository busRepository;
    private final BusRouteRepository busRouteRepository;

    // ========================== AUTO GENERATION ONLY ==========================
    public List<Schedule> generateSchedules(Long busId, LocalDate startDate, int days, String adminUserId) {

        // Validate bus ownership
        Bus bus = busRepository.findByIdAndAdminUserId(busId, adminUserId)
                .orElseThrow(() -> new RuntimeException("Bus not found"));

        // Load all active routes
        List<BusRoute> routes = busRouteRepository
                .findByBus_IdAndBus_AdminUserIdAndActiveTrue(busId, adminUserId);

        if (routes.isEmpty()) {
            throw new RuntimeException("No active routes for this bus.");
        }

        // Safety checks for input
        if (startDate == null) startDate = LocalDate.now();
        if (days <= 0) days = 1;

        List<Schedule> result = new ArrayList<>();

        // For each day
        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);
            // Check if this bus runs on this date (your rule)
            if (!shouldRun(bus, date))
                continue;

            // For each route
            for (BusRoute route : routes) {

                LocalDateTime departure = LocalDateTime.of(date, route.getDepartureTime());
                LocalDateTime arrival = departure.plusMinutes(
                        route.getEstimatedDuration() != null ? route.getEstimatedDuration() : 60
                );

                // Skip duplicates
                if (scheduleRepository.existsByBusAndRouteAndDepartureTime(bus, route, departure))
                    continue;

                Schedule schedule = new Schedule();
                schedule.setBus(bus);
                schedule.setRoute(route);
                schedule.setDepartureTime(departure);
                schedule.setArrivalTime(arrival);
                schedule.setPrice(route.getFinalFare());
                schedule.setAvailableSeats(bus.getTotalSeats());
                schedule.setActive(true);

                result.add(schedule);
            }
        }

        return scheduleRepository.saveAll(result);
    }

    private boolean shouldRun(Bus bus, LocalDate date) {
        switch (bus.getRecurrenceType()) {
            case DAILY: return true;
            case WEEKDAYS: return !isWeekend(date);
            case WEEKENDS: return isWeekend(date);
            case CUSTOM: return bus.getOperatingDays().contains(date.getDayOfWeek());
            case ALTERNATE: return date.getDayOfYear() % 2 == 0;
            default: return false;
        }
    }

    private boolean isWeekend(LocalDate date) {
        return date.getDayOfWeek() == DayOfWeek.SATURDAY ||
                date.getDayOfWeek() == DayOfWeek.SUNDAY;
    }

    // ========================== READ-ONLY QUERIES ==========================
    public List<Schedule> getSchedulesByBus(Long busId, String adminUserId) {
        return scheduleRepository.findByBus_IdAndBus_AdminUserId(busId, adminUserId);
    }

    public List<Schedule> getSchedulesByRoute(Long routeId, String adminUserId) {
        return scheduleRepository.findByRoute_IdAndBus_AdminUserId(routeId, adminUserId);
    }

    public Schedule getScheduleById(Long id, String adminUserId) {
        return scheduleRepository.findByIdAndBus_AdminUserId(id, adminUserId)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));
    }

    public List<Schedule> getSchedulesByDateRange(String adminUserId,
                                                  LocalDateTime start,
                                                  LocalDateTime end) {
        return scheduleRepository.findByBus_AdminUserIdAndDepartureTimeBetween(
                adminUserId, start, end
        );
    }

    // ========================== OPTIONAL ==========================
    public Schedule toggleScheduleStatus(Long id, String adminUserId) {
        Schedule schedule = getScheduleById(id, adminUserId);
        schedule.setActive(!schedule.getActive());
        return scheduleRepository.save(schedule);
    }

    public void deleteSchedule(Long id, String adminUserId) {
        Schedule schedule = getScheduleById(id, adminUserId);
        scheduleRepository.delete(schedule);
    }
}
