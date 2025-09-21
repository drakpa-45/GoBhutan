package com.goBhutan.adminPanel.busAdmin.service;

import com.goBhutan.adminPanel.busAdmin.dto.ScheduleRequest;
import com.goBhutan.adminPanel.busAdmin.entity.Bus;
import com.goBhutan.adminPanel.busAdmin.entity.Route;
import com.goBhutan.adminPanel.busAdmin.entity.Schedule;
import com.goBhutan.adminPanel.busAdmin.repository.BusRepository;
import com.goBhutan.adminPanel.busAdmin.repository.RouteRepository;
import com.goBhutan.adminPanel.busAdmin.repository.ScheduleRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ScheduleService {
    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private BusRepository busRepository;

    @Autowired
    private RouteRepository routeRepository;

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

    public List<Schedule> getSchedulesByBus(Long busId, String adminUserId) {
        // Avoid leaking other owners’ data
        return scheduleRepository.findByBus_IdAndBus_AdminUserId(busId, adminUserId);
    }

    public List<Schedule> getSchedulesByRoute(Long routeId, String adminUserId) {
        return scheduleRepository.findByRoute_IdAndBus_AdminUserId(routeId, adminUserId);
    }

    public Schedule getScheduleById(Long scheduleId, String adminUserId) {
        return scheduleRepository.findByIdAndBus_AdminUserId(scheduleId, adminUserId)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));
    }

    public List<Schedule> getSchedulesByDateRange(
            String adminUserId, LocalDateTime startDate, LocalDateTime endDate
    ) {
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
    }