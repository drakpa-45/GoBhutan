package com.goBhutan.adminPanel.theater.service;

import com.goBhutan.adminPanel.theater.dto.seat.*;
import com.goBhutan.adminPanel.theater.entity.Hall;
import com.goBhutan.adminPanel.theater.entity.Seat;
import com.goBhutan.adminPanel.theater.entity.SeatClass;
import com.goBhutan.adminPanel.theater.entity.SeatStatus;
import com.goBhutan.adminPanel.theater.layout.SeatLayoutRequest;
import com.goBhutan.adminPanel.theater.mapper.SeatMapper;
import com.goBhutan.adminPanel.theater.repository.HallRepository;
import com.goBhutan.adminPanel.theater.repository.SeatClassRepository;
import com.goBhutan.adminPanel.theater.repository.SeatRepository;
import com.goBhutan.adminPanel.theater.repository.SeatStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SeatService {

    private final SeatRepository seatRepository;
    private final HallRepository hallRepository;
    private final SeatClassRepository seatClassRepository;
    private final SeatStatusRepository seatStatusRepository;

    @Transactional
    public List<SeatClassDTO> getAllSeatClasses() {
        return seatClassRepository.findAll().stream()
                .map(this::mapToSeatClassDTO)
                .toList();
    }
    @Transactional
    public SeatClassDTO getSeatClassById(Long id) {
        SeatClass seatClass = seatClassRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Seat class not found with id: " + id));
        return mapToSeatClassDTO(seatClass);
    }

    private SeatClassDTO mapToSeatClassDTO(SeatClass seatClass) {
        SeatClassDTO dto = new SeatClassDTO();
        dto.setId(seatClass.getId());
        dto.setName(seatClass.getName());
        dto.setDescription(seatClass.getDescription());
        dto.setDefaultBasePrice(seatClass.getDefaultBasePrice());
        return dto;
    }

    public List<SeatStatusDTO> getAllSeatStatuses() {
        return seatStatusRepository.findAll().stream()
                .map(this::mapToSeatStatusDTO)
                .toList();
    }

    public SeatStatusDTO getSeatStatusById(Long id) {
        SeatStatus seatStatus = seatStatusRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Seat status not found with id: " + id));
        return mapToSeatStatusDTO(seatStatus);
    }

    public List<SeatStatusDTO> getActiveSeatStatuses() {
        return seatStatusRepository.findByIsActive(true).stream()
                .map(this::mapToSeatStatusDTO)
                .toList();
    }

    private SeatStatusDTO mapToSeatStatusDTO(SeatStatus seatStatus) {
        SeatStatusDTO dto = new SeatStatusDTO();
        dto.setId(seatStatus.getId());
        dto.setStatusName(seatStatus.getStatusName());
        dto.setDescription(seatStatus.getDescription());
        dto.setIsActive(seatStatus.getIsActive());
        dto.setCreatedAt(seatStatus.getCreatedAt());
        dto.setUpdatedAt(seatStatus.getUpdatedAt());
        return dto;
    }

    /**
     * Create / reset seat layout for a hall
     */
    @Transactional
    public SeatLayoutResponseDTO configureSeats(SeatLayoutRequest request) {
        log.info("Configuring seats for hall ID: {}", request.getHallId());

        Hall hall = hallRepository.findById(request.getHallId())
                .orElseThrow(() -> new IllegalArgumentException("Hall not found with ID: " + request.getHallId()));

        if (!hall.getIsActive()) {
            throw new IllegalArgumentException("Cannot configure seats for inactive hall");
        }

        SeatStatus availableStatus = seatStatusRepository.findByStatusNameIgnoreCase("AVAILABLE")
                .orElseThrow(() -> new IllegalArgumentException(
                        "Default seat status 'AVAILABLE' not found. Please initialize seat statuses first."));

        // Validate no duplicate row names
        Set<String> rowNames = new HashSet<>();
        for (SeatLayoutRequest.RowLayout row : request.getRows()) {
            if (!rowNames.add(row.getRowName().toUpperCase())) {
                throw new IllegalArgumentException("Duplicate row name: " + row.getRowName());
            }
        }

        // ✅ Delete existing seats — makes this safe to call for both create AND edit
        if (!hall.getSeats().isEmpty()) {
            log.info("Removing existing {} seats from hall {}", hall.getSeats().size(), hall.getId());
            seatRepository.deleteAllByHallId(hall.getId());
            hall.getSeats().clear();
        }

        int totalSeats = 0;

        for (SeatLayoutRequest.RowLayout row : request.getRows()) {
            SeatClass seatClass = seatClassRepository.findById(row.getSeatClassId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Seat class not found with ID: " + row.getSeatClassId()));

            for (int i = 1; i <= row.getSeatCount(); i++) {
                Seat seat = new Seat();
                seat.setRowName(row.getRowName().toUpperCase());
                seat.setSeatNumber(i);
                seat.setSeatClass(seatClass);
                seat.setStatus(availableStatus);
                seat.setBasePrice(row.getBasePrice());
                seat.setIsBlocked(false);
                seat.setCreatedAt(Instant.now());
                seat.setUpdatedAt(Instant.now());

                hall.addSeat(seat);
                totalSeats++;
            }
        }

        hall.setTotalSeats(totalSeats);
        hall.setUpdatedAt(Instant.now());

        Hall savedHall = hallRepository.save(hall);
        log.info("Configured {} seats for hall {}", totalSeats, hall.getId());

        return getSeatLayoutByHall(savedHall.getId());
    }

    /**
     * Get seat layout for a hall
     */
    @Transactional(readOnly = true)
    public SeatLayoutResponseDTO getSeatLayoutByHall(Long hallId) {
        Hall hall = hallRepository.findById(hallId)
                .orElseThrow(() -> new IllegalArgumentException("Hall not found with ID: " + hallId));

        List<Seat> seats = seatRepository.findByHallIdOrderByRowNameAscSeatNumberAsc(hallId);

        // Group seats by row
        Map<String, List<Seat>> seatsByRow = seats.stream()
                .collect(Collectors.groupingBy(Seat::getRowName, LinkedHashMap::new, Collectors.toList()));

        // Create row info
        List<SeatLayoutResponseDTO.RowLayout> rows = seatsByRow.entrySet().stream()
                .map(entry -> {
                    String rowName = entry.getKey();
                    List<Seat> rowSeats = entry.getValue();

                    List<SeatDTO> seatDTOs = rowSeats.stream()
                            .map(SeatMapper::toDTO)  // Updated mapper supports SeatClassEntity
                            .collect(Collectors.toList());

                    long blockedCount = rowSeats.stream()
                            .filter(s -> Boolean.TRUE.equals(s.getIsBlocked()))
                            .count();

                    return new SeatLayoutResponseDTO.RowLayout(
                            rowName,
                            seatDTOs,
                            rowSeats.size(),
                            (int) blockedCount
                    );
                })
                .collect(Collectors.toList());

        Map<String, Integer> seatClassCounts = seats.stream()
                .filter(s -> s.getSeatClass() != null)
                .collect(Collectors.groupingBy(
                        s -> s.getSeatClass().getName(),
                        Collectors.summingInt(s -> 1)
                ));

        long totalBlocked = seats.stream()
                .filter(s -> Boolean.TRUE.equals(s.getIsBlocked()))
                .count();

        SeatLayoutResponseDTO response = new SeatLayoutResponseDTO();
        response.setHallId(hall.getId());
        response.setHallName(hall.getName());
        response.setTotalSeats(seats.size());
        response.setBlockedSeats((int) totalBlocked);
        response.setAvailableSeats(seats.size() - (int) totalBlocked);
        response.setRows(rows);
        response.setSeatClassCounts(seatClassCounts);

        return response;
    }

    /**
     * List all seats of a hall
     */
    @Transactional(readOnly = true)
    public List<SeatDTO> getSeatsByHall(Long hallId) {
        return seatRepository.findByHallIdOrderByRowNameAscSeatNumberAsc(hallId)
                .stream()
                .map(SeatMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get seats by hall and row
     */
    @Transactional(readOnly = true)
    public List<SeatDTO> getSeatsByHallAndRow(Long hallId, String rowName) {
        return seatRepository.findByHallIdAndRowNameOrderBySeatNumberAsc(hallId, rowName.toUpperCase())
                .stream()
                .map(SeatMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Block or unblock a seat
     */
    public SeatDTO blockSeat(Long seatId, SeatBlockRequestDTO request) {
        log.info("Updating block status for seat ID: {} to {}", seatId, request.getBlock());

        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new IllegalArgumentException("Seat not found with ID: " + seatId));

        seat.setIsBlocked(request.getBlock());
        seat.setBlockReason(request.getBlock() ? request.getReason() : null);
        seat.setUpdatedAt(Instant.now());

        Seat updatedSeat = seatRepository.save(seat);
        log.info("Seat {} {} successfully", seat.getSeatIdentifier(),
                request.getBlock() ? "blocked" : "unblocked");

        return SeatMapper.toDTO(updatedSeat);
    }

    /**
     * Block multiple seats
     */
    public List<SeatDTO> blockMultipleSeats(List<Long> seatIds, SeatBlockRequestDTO request) {
        log.info("Blocking {} seats", seatIds.size());

        List<Seat> seats = seatRepository.findAllById(seatIds);

        if (seats.size() != seatIds.size()) {
            throw new IllegalArgumentException("Some seat IDs not found");
        }

        seats.forEach(seat -> {
            seat.setIsBlocked(request.getBlock());
            seat.setBlockReason(request.getBlock() ? request.getReason() : null);
            seat.setUpdatedAt(Instant.now());
        });

        List<Seat> updatedSeats = seatRepository.saveAll(seats);
        return updatedSeats.stream()
                .map(SeatMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get blocked seats by hall
     */
    @Transactional(readOnly = true)
    public List<SeatDTO> getBlockedSeatsByHall(Long hallId) {
        return seatRepository.findByHallIdAndIsBlockedTrue(hallId)
                .stream()
                .map(SeatMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get available seats by hall
     */
    @Transactional(readOnly = true)
    public List<SeatDTO> getAvailableSeatsByHall(Long hallId) {
        return seatRepository.findByHallIdAndIsBlockedFalse(hallId)
                .stream()
                .map(SeatMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get seat by identifier (e.g., "A5")
     */
    @Transactional(readOnly = true)
    public SeatDTO getSeatByIdentifier(Long hallId, String identifier) {
        // Parse identifier (e.g., "A5" -> row="A", number=5)
        String rowName = identifier.replaceAll("[0-9]", "").toUpperCase();
        String numberStr = identifier.replaceAll("[^0-9]", "");

        if (numberStr.isEmpty()) {
            throw new IllegalArgumentException("Invalid seat identifier: " + identifier);
        }

        int seatNumber = Integer.parseInt(numberStr);

        Seat seat = seatRepository.findByHallIdAndRowNameAndSeatNumber(hallId, rowName, seatNumber)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Seat not found: " + identifier + " in hall " + hallId
                ));

        return SeatMapper.toDTO(seat);
    }

    /**
     * Delete all seats from a hall
     */
    public void deleteAllSeatsFromHall(Long hallId) {
        log.info("Deleting all seats from hall ID: {}", hallId);

        Hall hall = hallRepository.findById(hallId)
                .orElseThrow(() -> new IllegalArgumentException("Hall not found with ID: " + hallId));

        seatRepository.deleteByHallId(hallId);
        hall.setTotalSeats(0);
        hall.setUpdatedAt(Instant.now());
        hallRepository.save(hall);

        log.info("All seats deleted from hall {}", hallId);
    }
}