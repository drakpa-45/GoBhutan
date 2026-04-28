package com.goBhutan.adminPanel.busAdmin.service;

import com.goBhutan.adminPanel.busAdmin.dto.BusRegistrationRequest;
import com.goBhutan.adminPanel.busAdmin.dto.BusResponseDTO;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
@Service
@Transactional
public class BusService {
    @Autowired
    private BusRepository busRepository;
    @Autowired
    private BusRouteRepository busRouteRepository;
    @Autowired
    private BusScheduleRepository scheduleRepository;
    @Autowired
    private SeatBookingRepository seatBookingRepository;

    public Bus registerBus(BusRegistrationRequest request, String adminUserId) {
        if (busRepository.findByBusNumber(request.getBusNumber()).isPresent()) {
            throw new RuntimeException("Bus number already exists");
        }

        Bus bus = new Bus();
        bus.setBusNumber(request.getBusNumber());
        bus.setBusName(request.getBusName());
        bus.setBusType(request.getBusType());
        bus.setTotalSeats(request.getTotalSeats());
        bus.setDescription(request.getDescription());
        bus.setAmenities(request.getAmenities());
        bus.setLayoutType(request.getLayoutType());
        bus.setAdminUserId(adminUserId);
        bus.setIsActive(true);

        applyScheduleRule(bus,request.getRecurrenceType() != null ? request.getRecurrenceType() : RecurrenceType.DAILY,
                request.getOperatingDays());
        applyScheduleAnchorDate(bus, request.getScheduleAnchorDate());

        return busRepository.save(bus);
    }

    public List<Bus> getBusesByOwner(String adminUserId) {
        return busRepository.findByAdminUserId(adminUserId);
    }

    public List<Bus> getActiveBuses() {
        return busRepository.findActiveBuses();
    }

    public Bus getBusById(Long busId, String adminUserId) {
        return busRepository.findByIdAndAdminUserId(busId, adminUserId)
                .orElseThrow(() -> new RuntimeException("Bus not found"));
    }

    public BusResponseDTO toDetailsDTO(Bus bus) {
        BusResponseDTO dto = new BusResponseDTO();

        dto.setId(bus.getId());
        dto.setBusNumber(bus.getBusNumber());
        dto.setBusName(bus.getBusName());
        dto.setBusType(bus.getBusType());
        dto.setTotalSeats(bus.getTotalSeats());
        dto.setDescription(bus.getDescription());
        dto.setAmenities(bus.getAmenities());
        dto.setAdminUserId(bus.getAdminUserId());
        dto.setLayoutType(bus.getLayoutType());
        dto.setIsActive(bus.getIsActive());
        dto.setRecurrenceType(bus.getRecurrenceType());
        dto.setScheduleAnchorDate(bus.getScheduleAnchorDate());
        dto.setOperatingDays(bus.getOperatingDays());

        // LAZY → ensure loaded
        dto.setSeats(new ArrayList<>(bus.getSeatConfigs()));
      //  dto.setBusRoutes(new ArrayList<>(bus.getBusRoutes()));
       // dto.setSchedules(new ArrayList<>(bus.getSchedules()));

        return dto;
    }

    public Bus updateBus(Long busId, BusRegistrationRequest request, String adminUserId) {
        Bus bus = getBusById(busId, adminUserId);

        // Check if bus number is being changed and if it already exists
        if (!bus.getBusNumber().equals(request.getBusNumber())) {
            if (busRepository.findByBusNumber(request.getBusNumber()).isPresent()) {
                throw new RuntimeException("Bus number already exists");
            }
        }

        bus.setBusNumber(request.getBusNumber());
        bus.setBusName(request.getBusName());
        bus.setBusType(request.getBusType());
        bus.setTotalSeats(request.getTotalSeats());
        bus.setDescription(request.getDescription());
        bus.setAmenities(request.getAmenities());
        bus.setLayoutType(request.getLayoutType());

        RecurrenceType recurrenceType = request.getRecurrenceType() != null
                ? request.getRecurrenceType()
                : (bus.getRecurrenceType() != null ? bus.getRecurrenceType() : RecurrenceType.DAILY);

        Set<DayOfWeek> operatingDays = request.getOperatingDays() != null
                ? request.getOperatingDays()
                : bus.getOperatingDays();
        applyScheduleRule(bus, recurrenceType, operatingDays);
        applyScheduleAnchorDate(bus, request.getScheduleAnchorDate());


        return busRepository.save(bus);
    }

    public void deleteBus(Long busId, String adminUserId) {
        Bus bus = getBusById(busId, adminUserId);
        List<BusRoute> activeRoutes = busRouteRepository.findByBus_IdAndBus_AdminUserIdAndActiveTrue(busId, adminUserId);
        bus.setIsActive(false);
        activeRoutes.forEach(route -> route.setActive(false));
        busRouteRepository.saveAll(activeRoutes);

        List<Schedule> futureSchedules = scheduleRepository
                .findByBus_IdAndBus_AdminUserIdAndActiveTrueAndDepartureTimeAfter(
                        busId, adminUserId, LocalDateTime.now());
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

        busRepository.save(bus);
    }

    private void applyScheduleRule(Bus bus,
                                   RecurrenceType recurrenceType,
                                   Set<DayOfWeek> operatingDays) {
        bus.setRecurrenceType(recurrenceType);

        if (recurrenceType == RecurrenceType.CUSTOM) {
            if (operatingDays == null || operatingDays.isEmpty()) {
                throw new RuntimeException("Operating days are required for CUSTOM bus recurrence");
            }
            bus.setOperatingDays(new HashSet<>(operatingDays));
            return;
        }

        if (bus.getOperatingDays() == null) {
            bus.setOperatingDays(new HashSet<>());
        } else {
            bus.getOperatingDays().clear();
        }
    }

    private void applyScheduleAnchorDate(Bus bus, LocalDate requestedAnchorDate) {
        if (requestedAnchorDate != null) {
            bus.setScheduleAnchorDate(requestedAnchorDate);
            return;
        }

        if (bus.getScheduleAnchorDate() == null) {
            bus.setScheduleAnchorDate(LocalDate.now());
        }
    }

}
