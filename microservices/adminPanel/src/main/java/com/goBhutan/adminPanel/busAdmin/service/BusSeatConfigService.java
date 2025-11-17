package com.goBhutan.adminPanel.busAdmin.service;

import com.goBhutan.adminPanel.busAdmin.entity.Bus;
import com.goBhutan.adminPanel.busAdmin.entity.BusSeatConfig;
import com.goBhutan.adminPanel.busAdmin.enums.SeatType;
import com.goBhutan.adminPanel.busAdmin.repository.BusRepository;
import com.goBhutan.adminPanel.busAdmin.repository.BusSeatConfigRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class BusSeatConfigService {
    @Autowired
    private BusRepository busRepository;
    @Autowired
    private BusSeatConfigRepository seatConfigRepository;

    private static final Logger log = LoggerFactory.getLogger(BusSeatConfigService.class);


    public List<BusSeatConfig> getConfigsByBus(Long busId) {
        return seatConfigRepository.findByBus_Id(busId);
    }


    @Transactional
    public List<BusSeatConfig> generateSeatLayout(Long busId, boolean forceRegenerate) {
        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new RuntimeException("Bus not found with ID: " + busId));

        Integer totalSeats = bus.getTotalSeats();
        if (totalSeats == null || totalSeats < 4) {
            throw new IllegalArgumentException("Invalid seat count. Total seats must be 4 or more.");
        }

        String layoutType = (bus.getLayoutType() != null && !bus.getLayoutType().isBlank())
                ? bus.getLayoutType().trim()
                : "2+2"; // Default fallback

        Layout layout = parseLayout(layoutType);

        if (layout.left <= 0 || layout.right <= 0) {
            throw new IllegalArgumentException("Invalid layout type '" + layoutType +
                    "'. Both left and right sides must have at least one seat.");
        }

        int seatsPerRow = layout.left + layout.right;
        if (seatsPerRow < 2) {
            throw new IllegalArgumentException("Layout must have at least two seats per row.");
        }

        int totalRows = (int) Math.ceil((double) totalSeats / seatsPerRow);
        if (totalRows < 1) {
            throw new IllegalArgumentException("Calculated total rows cannot be less than 1.");
        }

        List<BusSeatConfig> existing = seatConfigRepository.findByBus_Id(busId);
        if (!existing.isEmpty()) {
            if (forceRegenerate) {
                seatConfigRepository.deleteAll(existing);
            } else {
                throw new IllegalStateException("Seat layout already exists for bus ID " + busId +
                        ". Use forceRegenerate=true to overwrite.");
            }
        }

        List<BusSeatConfig> configs = new ArrayList<>();
        int seatNo = 1;

        for (int row = 1; row <= totalRows && seatNo <= totalSeats; row++) {
            for (int col = 1; col <= seatsPerRow && seatNo <= totalSeats; col++) {

                SeatType seatType = determineSeatType(row, col, layout, totalRows);
                String seatLabel = row + getColumnLetter(col);

                if (seatLabel == null || seatLabel.isBlank()) {
                    throw new IllegalArgumentException("Generated seat label is invalid for seat #" + seatNo);
                }

                BusSeatConfig cfg = new BusSeatConfig();
                cfg.setBus(bus);
                cfg.setStartNo(seatNo);
                cfg.setEndNo(seatNo);
                cfg.setSeatLabel(seatLabel);
                cfg.setSeatType(seatType);

                configs.add(cfg);
                seatNo++;
            }
        }

        validateSeatConfigs(configs, bus);

        List<BusSeatConfig> saved = seatConfigRepository.saveAll(configs);

        log.info("✅ Generated {} seat configurations for bus {} (layout: {}, totalRows: {})",
                saved.size(), bus.getBusNumber(), layoutType, totalRows);

        return saved;
    }

    private Layout parseLayout(String layoutType) {
        try {
            String[] parts = layoutType.split("\\+");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid layout format: '" + layoutType +
                        "'. Expected format like '1+2' or '2+3'.");
            }

            int left = Integer.parseInt(parts[0]);
            int right = Integer.parseInt(parts[1]);

            if (left <= 0 || right <= 0) {
                throw new IllegalArgumentException("Both left and right layout parts must be positive integers.");
            }

            return new Layout(left, right);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid layout type: '" + layoutType +
                    "'. Expected numeric format (e.g., '2+2').");
        }
    }

    private void validateSeatConfigs(List<BusSeatConfig> configs, Bus bus) {
        Set<Integer> seenSeats = new HashSet<>();
        int maxSeats = bus.getTotalSeats();

        for (BusSeatConfig c : configs) {
            int seatNo = c.getStartNo();

            if (seatNo < 1 || seatNo > maxSeats) {
                throw new IllegalArgumentException("Seat number " + seatNo +
                        " is outside valid range (1–" + maxSeats + ")");
            }

            if (!seenSeats.add(seatNo)) {
                throw new IllegalArgumentException("Duplicate seat number detected: " + seatNo);
            }

            if (c.getSeatType() == null) {
                throw new IllegalArgumentException("Seat type cannot be null for seat number " + seatNo);
            }
        }
    }

    // Determine seat type dynamically
    private SeatType determineSeatType(int row, int col, Layout layout, int totalRows) {
        if (row == 1) {
            if (col == 1 || col == layout.left + layout.right)
                return SeatType.FRONT_WINDOW;
            return SeatType.FRONT;
        } else if (row == totalRows) {
            if (col == 1 || col == layout.left + layout.right)
                return SeatType.BACK_WINDOW;
            return SeatType.BACK;
        } else {
            if (col == 1 || col == layout.left + layout.right)
                return SeatType.WINDOW;
            return SeatType.AISLE;
        }
    }

    private String getColumnLetter(int col) {
        return String.valueOf((char) ('A' + (col - 1)));
    }

    private record Layout(int left, int right) {}




   /* public BusSeatConfig addConfig(Long busId, BusSeatConfig config) {
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
    }*/
}