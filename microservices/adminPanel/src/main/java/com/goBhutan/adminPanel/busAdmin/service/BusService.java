package com.goBhutan.adminPanel.busAdmin.service;

import com.goBhutan.adminPanel.busAdmin.dto.BusRegistrationRequest;
import com.goBhutan.adminPanel.busAdmin.dto.BusResponseDTO;
import com.goBhutan.adminPanel.busAdmin.entity.Bus;
import com.goBhutan.adminPanel.busAdmin.enums.RecurrenceType;
import com.goBhutan.adminPanel.busAdmin.repository.BusRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
@Service
@Transactional
public class BusService {
    @Autowired
    private BusRepository busRepository;

    public Bus registerBus(BusRegistrationRequest request, String adminUserId) {
        if (busRepository.findByBusNumber(request.getBusNumber()).isPresent()) {
            throw new RuntimeException("Bus number already exists");
        }

        Bus bus = new Bus();
        bus.setBusNumber(request.getBusNumber());
        bus.setBusType(request.getBusType());
        bus.setTotalSeats(request.getTotalSeats());
        bus.setDescription(request.getDescription());
        bus.setAmenities(request.getAmenities());
        bus.setLayoutType(request.getLayoutType());
        bus.setAdminUserId(adminUserId);

        bus.setRecurrenceType(request.getRecurrenceType() != null
                ? request.getRecurrenceType()
                : RecurrenceType.DAILY);

        if (bus.getRecurrenceType() == RecurrenceType.CUSTOM) {
            bus.setOperatingDays(request.getOperatingDays() != null
                    ? request.getOperatingDays()
                    : new HashSet<>());
        }

        return busRepository.save(bus);
    }

    public List<Bus> getBusesByOwner(String adminUserId) {
        return busRepository.findByAdminUserId(adminUserId);
    }

    public Bus getBusById(Long busId, String adminUserId) {
        return busRepository.findByIdAndAdminUserId(busId, adminUserId)
                .orElseThrow(() -> new RuntimeException("Bus not found"));
    }

    public BusResponseDTO toDetailsDTO(Bus bus) {
        BusResponseDTO dto = new BusResponseDTO();

        dto.setId(bus.getId());
        dto.setBusNumber(bus.getBusNumber());
        dto.setBusType(bus.getBusType());
        dto.setTotalSeats(bus.getTotalSeats());
        dto.setDescription(bus.getDescription());
        dto.setAmenities(bus.getAmenities());
        dto.setAdminUserId(bus.getAdminUserId());
        dto.setLayoutType(bus.getLayoutType());
        //dto.setRecurrenceType(bus.getRecurrenceType());
        //dto.setOperatingDays(bus.getOperatingDays());

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
        bus.setBusType(request.getBusType());
        bus.setTotalSeats(request.getTotalSeats());
        bus.setDescription(request.getDescription());
        bus.setAmenities(request.getAmenities());

        bus.setRecurrenceType(request.getRecurrenceType());

        if (request.getRecurrenceType() == RecurrenceType.CUSTOM) {
            bus.setOperatingDays(request.getOperatingDays());
        } else {
            bus.getOperatingDays().clear();
        }


        return busRepository.save(bus);
    }

    public void deleteBus(Long busId, String adminUserId) {
        Bus bus = getBusById(busId, adminUserId);
        busRepository.delete(bus);
    }


}
