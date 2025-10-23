package com.goBhutan.adminPanel.busAdmin.service;

import com.goBhutan.adminPanel.busAdmin.dto.SeatType;
import com.goBhutan.adminPanel.busAdmin.entity.Bus;
import com.goBhutan.adminPanel.busAdmin.entity.BusSeat;
import com.goBhutan.adminPanel.busAdmin.entity.BusSeatConfig;
import com.goBhutan.adminPanel.busAdmin.repository.BusRepository;
import com.goBhutan.adminPanel.busAdmin.repository.BusSeatConfigRepository;
import com.goBhutan.adminPanel.busAdmin.repository.BusSeatRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class BusSeatService {
    @Autowired
    private  BusRepository busRepository;
    @Autowired
    private  BusSeatRepository seatRepository;
    @Autowired
    private  BusSeatConfigRepository seatConfigRepository;


    public List<BusSeat> generateSeatsForBus(Long busId) {
        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new RuntimeException("Bus not found"));

        int totalSeats = bus.getTotalSeats();
        List<BusSeatConfig> configs = seatConfigRepository.findByBus_Id(busId);

        if (configs.isEmpty()) {
            throw new RuntimeException("No seat configuration found for this bus.");
        }

        List<BusSeat> seats = new ArrayList<>();

        for (int i = 1; i <= totalSeats; i++) {
            BusSeat seat = new BusSeat();
            seat.setSeatNumber("S" + i);
            seat.setSeatType(determineSeatTypeFromConfig(configs, i));
            seat.setReserved(false);
            seat.setBus(bus);
            seats.add(seat);
        }

        return seatRepository.saveAll(seats);
    }

    private SeatType determineSeatTypeFromConfig(List<BusSeatConfig> configs, int seatNumber) {
        for (BusSeatConfig config : configs) {
            if (seatNumber >= config.getStartNo() && seatNumber <= config.getEndNo()) {
                return config.getSeatType();
            }
        }
        return SeatType.AISLE; // Default
    }

    public List<BusSeat> getSeatsByBus(Long busId) {
        return seatRepository.findByBus_Id(busId);
    }

    public void deleteAllSeatsByBus(Long busId) {
        List<BusSeat> seats = seatRepository.findByBus_Id(busId);
        seatRepository.deleteAll(seats);
    }
}

