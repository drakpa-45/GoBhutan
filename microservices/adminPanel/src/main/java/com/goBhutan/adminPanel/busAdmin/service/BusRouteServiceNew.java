package com.goBhutan.adminPanel.busAdmin.service;

import com.goBhutan.adminPanel.busAdmin.dto.BusRouteRequest;
import com.goBhutan.adminPanel.busAdmin.dto.BusRouteResponse;
import com.goBhutan.adminPanel.busAdmin.entity.Bus;
import com.goBhutan.adminPanel.busAdmin.entity.BusRoute;
import com.goBhutan.adminPanel.busAdmin.entity.Schedule;
import com.goBhutan.adminPanel.busAdmin.enums.BookingStatus;
import com.goBhutan.adminPanel.busAdmin.repository.BusRepository;
import com.goBhutan.adminPanel.busAdmin.repository.BusRouteRepository;
import com.goBhutan.adminPanel.busAdmin.repository.BusScheduleRepository;
import com.goBhutan.adminPanel.busAdmin.repository.SeatBookingRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class BusRouteServiceNew {

    private static final int CHECK_IN_OFFSET_MINUTES = 30;
    private static final DateTimeFormatter TIME_DISPLAY_FORMATTER =
            DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);

    private final BusRepository busRepository;
    private final BusRouteRepository busRouteRepository;
    private final BusScheduleRepository scheduleRepository;
    private final SeatBookingRepository seatBookingRepository;

    // ========================= CREATE ===============================
    public BusRouteResponse createRoute(BusRouteRequest req, String adminUserId) {

        Bus bus = busRepository.findByIdAndAdminUserId(req.getBusId(), adminUserId)
                .orElseThrow(() -> new RuntimeException("Bus not found"));
        String source = normalizeLocation(req.getSource(), "Source");
        String destination = normalizeLocation(req.getDestination(), "Destination");
        LocalTime departureTime = normalizeDepartureTime(req.getDepartureTime());
        Integer estimatedDurationMinutes = normalizeEstimatedDurationMinutes(req.getEstimatedDurationMinutes());

        // Prevent duplicate
        boolean exists = busRouteRepository.existsByBusAndDepartureTimeAndSourceAndDestinationAndActiveTrue(
                bus, departureTime, source, destination);

        if (exists) {
            throw new RuntimeException("Route already exists for this bus at the specified departure time.");
        }

        validateFare(req.getBaseFare(), req.getAppCharges());

        BusRoute br = new BusRoute();
        br.setBus(bus);
        br.setSource(source);
        br.setDestination(destination);
        br.setDistance(req.getDistance());
        br.setBaseFare(req.getBaseFare());
        br.setEstimatedDurationMinutes(estimatedDurationMinutes);
        br.setDepartureTime(departureTime);
        br.setCheckInTime(calculateCheckInTime(departureTime));
        br.setAppCharges(normalizeAppCharges(req.getAppCharges()));
        br.setActive(req.getActive() != null ? req.getActive() : true);

        return toResponse(busRouteRepository.save(br));
    }

    // ========================= UPDATE ===============================

    public BusRouteResponse updateRoute(Long id, BusRouteRequest req, String adminUserId) {

        BusRoute br = busRouteRepository.findByIdAndBus_AdminUserId(id, adminUserId)
                .orElseThrow(() -> new RuntimeException("Route not found"));
        String source = normalizeLocation(req.getSource(), "Source");
        String destination = normalizeLocation(req.getDestination(), "Destination");
        LocalTime departureTime = normalizeDepartureTime(req.getDepartureTime());
        Integer estimatedDurationMinutes = normalizeEstimatedDurationMinutes(req.getEstimatedDurationMinutes());
        if (Boolean.FALSE.equals(br.getBus().getIsActive())) {
            throw new RuntimeException("Bus is not active");
        }

        if (!br.getBus().getId().equals(req.getBusId())) {
            throw new RuntimeException("Route cannot be moved to a different bus");
        }

        boolean wasActive = Boolean.TRUE.equals(br.getActive());
        boolean willBeActive = req.getActive() != null ? req.getActive() : br.getActive();
        if (willBeActive && busRouteRepository.existsByBusAndDepartureTimeAndSourceAndDestinationAndActiveTrueAndIdNot(
                br.getBus(),
                departureTime,
                source,
                destination,
                br.getId()
        )) {
            throw new RuntimeException("Route already exists for this bus at the specified departure time.");
        }

        validateFare(req.getBaseFare(), req.getAppCharges());

        br.setSource(source);
        br.setDestination(destination);
        br.setDistance(req.getDistance());
        br.setBaseFare(req.getBaseFare());
        br.setEstimatedDurationMinutes(estimatedDurationMinutes);
        br.setDepartureTime(departureTime);
        br.setCheckInTime(calculateCheckInTime(departureTime));
        br.setAppCharges(normalizeAppCharges(req.getAppCharges()));
        br.setActive(willBeActive);
        if (wasActive && !Boolean.TRUE.equals(willBeActive)) {
            deactivateFutureUnbookedSchedules(br, adminUserId);
        }

        return toResponse(busRouteRepository.save(br));
    }

    // ========================= SOFT DELETE ===========================

    public void softDeleteRoute(Long id, String adminUserId) {
        BusRoute route = busRouteRepository.findByIdAndBus_AdminUserId(id, adminUserId)
                .orElseThrow(() -> new RuntimeException("Route not found"));

        route.setActive(false);
        deactivateFutureUnbookedSchedules(route, adminUserId);
        busRouteRepository.save(route);
    }

    // ========================= GET ONE ===============================

    public BusRouteResponse getRoute(Long id, String adminUserId) {
        BusRoute route = busRouteRepository.findByIdAndBus_AdminUserId(id, adminUserId)
                .orElseThrow(() -> new RuntimeException("Route not found"));

        return toResponse(route);
    }

    // ========================= GET ALL FOR BUS =======================

    public List<BusRouteResponse> getRoutesByBus(Long busId, String adminUserId) {

        return busRouteRepository.findByBus_IdAndBus_AdminUserIdAndActiveTrue(busId, adminUserId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<BusRouteResponse> getActiveRoutesBySourceAndDestination(String source, String destination) {
        return getActiveRoutesBySourceAndDestination(source, destination, null);
    }

    public List<BusRouteResponse> getActiveRoutesBySourceAndDestination(String source,
                                                                        String destination,
                                                                        LocalDate travelDate) {
        String normalizedSource = normalizeLocation(source, "Source");
        String normalizedDestination = normalizeLocation(destination, "Destination");
        LocalDateTime now = LocalDateTime.now();

        List<Schedule> schedules;
        if (travelDate != null) {
            if (travelDate.isBefore(LocalDate.now())) {
                throw new RuntimeException("Travel date cannot be in the past");
            }

            schedules = scheduleRepository.findBookableAppSchedulesBySourceDestinationAndDate(
                    normalizedSource,
                    normalizedDestination,
                    travelDate.atStartOfDay(),
                    travelDate.atTime(LocalTime.MAX),
                    now);
        } else {
            schedules = scheduleRepository.findBookableAppSchedulesBySourceAndDestination(
                    normalizedSource,
                    normalizedDestination,
                    now);
        }

        return selectNextSchedulePerRoute(schedules)
                .stream()
                .map(this::toResponseWithSchedule)
                .collect(Collectors.toList());
    }

    public List<BusRouteResponse> getBookableSchedulesBySourceDestinationAndDate(String source,
                                                                                 String destination,
                                                                                 LocalDate travelDate) {
        if (travelDate == null) {
            throw new RuntimeException("Travel date is required");
        }
        if (travelDate.isBefore(LocalDate.now())) {
            throw new RuntimeException("Travel date cannot be in the past");
        }

        String normalizedSource = normalizeLocation(source, "Source");
        String normalizedDestination = normalizeLocation(destination, "Destination");
        LocalDateTime now = LocalDateTime.now();

        return scheduleRepository.findBookableAppSchedulesBySourceDestinationAndDate(
                        normalizedSource,
                        normalizedDestination,
                        travelDate.atStartOfDay(),
                        travelDate.atTime(LocalTime.MAX),
                        now)
                .stream()
                .map(this::toResponseWithSchedule)
                .collect(Collectors.toList());
    }

    // ========================= MAPPER ================================
    private BusRouteResponse toResponse(BusRoute br) {
        return new BusRouteResponse(
                br.getId(),
                br.getBus().getId(),
                br.getBus().getBusNumber(),
                br.getSource(),
                br.getDestination(),
                br.getDistance(),
                br.getBaseFare(),
                br.getAppCharges(),
                br.getFinalFare(),
                br.getEstimatedDurationMinutes(),
                br.getDepartureTime(),
                formatDisplayTime(br.getDepartureTime()),
                getCheckInTime(br),
                formatDisplayTime(getCheckInTime(br)),
                br.getActive(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    private BusRouteResponse toResponseWithSchedule(Schedule schedule) {
        BusRoute route = schedule.getRoute();
        LocalDateTime scheduledCheckInTime = calculateCheckInTime(schedule.getDepartureTime());

        return new BusRouteResponse(
                route.getId(),
                route.getBus().getId(),
                route.getBus().getBusNumber(),
                route.getSource(),
                route.getDestination(),
                route.getDistance(),
                schedule.getBaseFare(),
                schedule.getAppCharges(),
                schedule.getFinalFare(),
                route.getEstimatedDurationMinutes(),
                route.getDepartureTime(),
                formatDisplayTime(route.getDepartureTime()),
                getCheckInTime(route),
                formatDisplayTime(getCheckInTime(route)),
                route.getActive(),
                schedule.getId(),
                schedule.getDepartureTime() != null ? schedule.getDepartureTime().toLocalDate() : null,
                scheduledCheckInTime,
                formatDisplayTime(scheduledCheckInTime),
                schedule.getDepartureTime(),
                formatDisplayTime(schedule.getDepartureTime()),
                schedule.getArrivalTime(),
                formatDisplayTime(schedule.getArrivalTime()),
                schedule.getAvailableSeats()
        );
    }

    private List<Schedule> selectNextSchedulePerRoute(List<Schedule> schedules) {
        Map<Long, Schedule> schedulesByRoute = new LinkedHashMap<>();
        for (Schedule schedule : schedules) {
            if (schedule.getRoute() != null) {
                schedulesByRoute.putIfAbsent(schedule.getRoute().getId(), schedule);
            }
        }
        return new ArrayList<>(schedulesByRoute.values());
    }

    private void deactivateFutureUnbookedSchedules(BusRoute route, String adminUserId) {
        List<Schedule> futureSchedules = scheduleRepository
                .findByRoute_IdAndBus_AdminUserIdAndActiveTrueAndDepartureTimeAfter(
                        route.getId(), adminUserId, LocalDateTime.now());

        List<Schedule> schedulesToDeactivate = new ArrayList<>();
        for (Schedule schedule : futureSchedules) {
            if (!seatBookingRepository.existsByScheduleIdAndStatus(schedule.getId(), BookingStatus.BOOKED)) {
                schedule.setActive(false);
                schedulesToDeactivate.add(schedule);
            }
        }

        if (!schedulesToDeactivate.isEmpty()) {
            scheduleRepository.saveAll(schedulesToDeactivate);
        }
    }

    private void validateFare(BigDecimal baseFare, BigDecimal appCharges) {
        if (baseFare == null || baseFare.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Base fare must be greater than 0");
        }
        if (appCharges != null && appCharges.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("App charges must be zero or greater");
        }
    }

    private BigDecimal normalizeAppCharges(BigDecimal appCharges) {
        return appCharges != null ? appCharges : BigDecimal.ZERO;
    }

    private LocalTime normalizeDepartureTime(LocalTime departureTime) {
        if (departureTime == null) {
            throw new RuntimeException("Departure time is required");
        }
        return departureTime;
    }

    private Integer normalizeEstimatedDurationMinutes(Integer estimatedDurationMinutes) {
        if (estimatedDurationMinutes == null) {
            throw new RuntimeException("Estimated duration in minutes is required");
        }
        if (estimatedDurationMinutes <= 0) {
            throw new RuntimeException("Estimated duration in minutes must be at least 1");
        }
        return estimatedDurationMinutes;
    }

    private LocalTime calculateCheckInTime(LocalTime departureTime) {
        return departureTime.minusMinutes(CHECK_IN_OFFSET_MINUTES);
    }

    private LocalDateTime calculateCheckInTime(LocalDateTime departureTime) {
        return departureTime != null ? departureTime.minusMinutes(CHECK_IN_OFFSET_MINUTES) : null;
    }

    private LocalTime getCheckInTime(BusRoute route) {
        if (route.getCheckInTime() != null) {
            return route.getCheckInTime();
        }
        return route.getDepartureTime() != null ? calculateCheckInTime(route.getDepartureTime()) : null;
    }

    private String formatDisplayTime(LocalTime time) {
        return time != null ? time.format(TIME_DISPLAY_FORMATTER) : null;
    }

    private String formatDisplayTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(TIME_DISPLAY_FORMATTER) : null;
    }

    private String normalizeLocation(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new RuntimeException(fieldName + " is required");
        }
        return value.trim();
    }
}
