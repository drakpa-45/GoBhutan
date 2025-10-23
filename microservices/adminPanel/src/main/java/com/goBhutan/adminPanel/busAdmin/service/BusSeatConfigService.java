package com.goBhutan.adminPanel.busAdmin.service;

import com.goBhutan.adminPanel.busAdmin.entity.Bus;
import com.goBhutan.adminPanel.busAdmin.entity.BusSeatConfig;
import com.goBhutan.adminPanel.busAdmin.repository.BusRepository;
import com.goBhutan.adminPanel.busAdmin.repository.BusSeatConfigRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class BusSeatConfigService {
    @Autowired
    private BusRepository busRepository;
    @Autowired
    private BusSeatConfigRepository seatConfigRepository;

    public List<BusSeatConfig> getConfigsByBus(Long busId) {
        return seatConfigRepository.findByBus_Id(busId);
    }

    public BusSeatConfig addConfig(Long busId, BusSeatConfig config) {
        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new RuntimeException("Bus not found"));

        validateConfig(bus, config, null);
        config.setBus(bus);
        return seatConfigRepository.save(config);
    }

    public BusSeatConfig updateConfig(Long configId, BusSeatConfig updated) {
        BusSeatConfig existing = seatConfigRepository.findById(configId)
                .orElseThrow(() -> new RuntimeException("Seat configuration not found"));

        validateConfig(existing.getBus(), updated, configId);

        existing.setStartNo(updated.getStartNo());
        existing.setEndNo(updated.getEndNo());
        existing.setSeatType(updated.getSeatType());
        return seatConfigRepository.save(existing);
    }

    public void deleteConfig(Long configId) {
        seatConfigRepository.deleteById(configId);
    }

    private void validateConfig(Bus bus, BusSeatConfig config, Long excludeConfigId) {
        int totalSeats = bus.getTotalSeats();

        if (config.getStartNo() == null || config.getEndNo() == null) {
            throw new IllegalArgumentException("Seat range (startNo and endNo) cannot be null.");
        }

        if (config.getStartNo() > config.getEndNo()) {
            throw new IllegalArgumentException("startNo cannot be greater than endNo.");
        }

        if (config.getStartNo() < 1 || config.getEndNo() > totalSeats) {
            throw new IllegalArgumentException("Seat range must be within 1 and total seat count (" + totalSeats + ").");
        }

        List<BusSeatConfig> existingConfigs = seatConfigRepository.findByBus_Id(bus.getId());

        for (BusSeatConfig existing : existingConfigs) {
            if (excludeConfigId != null && existing.getId().equals(excludeConfigId)) continue;

            boolean overlaps = config.getStartNo() <= existing.getEndNo() &&
                    config.getEndNo() >= existing.getStartNo();

            if (overlaps) {
                throw new IllegalArgumentException(String.format(
                        "Seat range %d–%d overlaps with existing range %d–%d",
                        config.getStartNo(), config.getEndNo(),
                        existing.getStartNo(), existing.getEndNo()
                ));
            }
        }
    }
}