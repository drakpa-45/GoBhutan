package com.goBhutan.adminPanel.theater.service;

import com.goBhutan.adminPanel.theater.dto.hall.HallDTO;
import com.goBhutan.adminPanel.theater.dto.hall.HallResponseDTO;
import com.goBhutan.adminPanel.theater.dto.hall.HallSummaryDTO;
import com.goBhutan.adminPanel.theater.dto.hall.HallUpdateDTO;
import com.goBhutan.adminPanel.theater.entity.Hall;
import com.goBhutan.adminPanel.theater.entity.Theater;
import com.goBhutan.adminPanel.theater.mapper.HallMapper;
import com.goBhutan.adminPanel.theater.repository.HallRepository;
import com.goBhutan.adminPanel.theater.repository.TheaterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class HallService {

    private final HallRepository hallRepository;
    private final TheaterRepository theaterRepository;

    /**
     * Create a new hall
     */
    public HallResponseDTO createHall(HallDTO hallDTO) {
        log.info("Creating hall: {} for theater ID: {}", hallDTO.getName(), hallDTO.getTheaterId());

        // Fetch theater
        Theater theater = theaterRepository.findById(hallDTO.getTheaterId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Theater not found with ID: " + hallDTO.getTheaterId()
                ));

        // Check if theater is active
        if (!theater.getIsActive()) {
            throw new IllegalArgumentException("Cannot create hall for inactive theater");
        }

        // Check for duplicate hall name in the same theater
        if (hallRepository.existsByNameAndTheaterId(hallDTO.getName(), hallDTO.getTheaterId())) {
            throw new IllegalArgumentException(
                    "Hall with name '" + hallDTO.getName() + "' already exists in this theater"
            );
        }

        // Create hall entity
        Hall hall = HallMapper.toEntity(hallDTO);
        hall.setTheater(theater);
        hall.setIsActive(true);
        hall.setCreatedAt(Instant.now());
        hall.setUpdatedAt(Instant.now());

        // Save hall
        Hall savedHall = hallRepository.save(hall);
        log.info("Hall created successfully with ID: {}", savedHall.getId());

        return HallMapper.toResponseDTO(savedHall);
    }

    /**
     * Update an existing hall
     */
    public HallResponseDTO updateHall(Long hallId, HallUpdateDTO updateDTO) {
        log.info("Updating hall with ID: {}", hallId);

        // Find existing hall
        Hall hall = hallRepository.findById(hallId)
                .orElseThrow(() -> new IllegalArgumentException("Hall not found with ID: " + hallId));

        // Check for duplicate name if name is being updated
        if (updateDTO.getName() != null && !updateDTO.getName().equals(hall.getName())) {
            Long theaterId = updateDTO.getTheaterId() != null ?
                    updateDTO.getTheaterId() : hall.getTheater().getId();

            if (hallRepository.existsByNameAndTheaterId(updateDTO.getName(), theaterId)) {
                throw new IllegalArgumentException(
                        "Hall with name '" + updateDTO.getName() + "' already exists in this theater"
                );
            }
        }

        // Update theater if provided
        if (updateDTO.getTheaterId() != null && !updateDTO.getTheaterId().equals(hall.getTheater().getId())) {
            Theater newTheater = theaterRepository.findById(updateDTO.getTheaterId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Theater not found with ID: " + updateDTO.getTheaterId()
                    ));

            if (!newTheater.getIsActive()) {
                throw new IllegalArgumentException("Cannot move hall to inactive theater");
            }

            hall.setTheater(newTheater);
        }

        // Update other fields
        HallMapper.updateEntityFromDTO(hall, updateDTO);
        hall.setUpdatedAt(Instant.now());

        // Save updated hall
        Hall updatedHall = hallRepository.save(hall);
        log.info("Hall updated successfully: {}", hallId);

        return HallMapper.toResponseDTO(updatedHall);
    }

    /**
     * Soft delete a hall
     */
    public void softDeleteHall(Long hallId) {
        log.info("Soft deleting hall with ID: {}", hallId);

        Hall hall = hallRepository.findById(hallId)
                .orElseThrow(() -> new IllegalArgumentException("Hall not found with ID: " + hallId));

        hall.setIsActive(false);
        hall.setUpdatedAt(Instant.now());

        hallRepository.save(hall);
        log.info("Hall soft deleted successfully: {}", hallId);
    }

    /**
     * Hard delete a hall
     */
    public void deleteHall(Long hallId) {
        log.info("Deleting hall with ID: {}", hallId);

        Hall hall = hallRepository.findById(hallId)
                .orElseThrow(() -> new IllegalArgumentException("Hall not found with ID: " + hallId));

        // Check if hall has seats
        if (hall.getSeats() != null && !hall.getSeats().isEmpty()) {
            throw new IllegalArgumentException(
                    "Cannot delete hall with " + hall.getSeats().size() +
                            " seats. Please remove seats first or use soft delete."
            );
        }

        hallRepository.delete(hall);
        log.info("Hall deleted successfully: {}", hallId);
    }

    /**
     * Restore soft deleted hall
     */
    public void restoreHall(Long hallId) {
        log.info("Restoring hall with ID: {}", hallId);

        Hall hall = hallRepository.findById(hallId)
                .orElseThrow(() -> new IllegalArgumentException("Hall not found with ID: " + hallId));

        // Check if theater is active
        if (!hall.getTheater().getIsActive()) {
            throw new IllegalArgumentException("Cannot restore hall of inactive theater");
        }

        hall.setIsActive(true);
        hall.setUpdatedAt(Instant.now());

        hallRepository.save(hall);
        log.info("Hall restored successfully: {}", hallId);
    }

    /**
     * Get hall by ID
     */
    @Transactional(readOnly = true)
    public HallResponseDTO getHallById(Long hallId) {
        Hall hall = hallRepository.findById(hallId)
                .orElseThrow(() -> new IllegalArgumentException("Hall not found with ID: " + hallId));

        return HallMapper.toResponseDTO(hall);
    }

    /**
     * Get all active halls
     */
    @Transactional(readOnly = true)
    public List<HallSummaryDTO> getAllActiveHalls() {
        return hallRepository.findByIsActiveTrue().stream()
                .map(HallMapper::toSummaryDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get halls by theater
     */
    @Transactional(readOnly = true)
    public List<HallSummaryDTO> getHallsByTheater(Long theaterId) {
        return hallRepository.findByTheaterIdAndIsActiveTrue(theaterId).stream()
                .map(HallMapper::toSummaryDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all halls by theater (including inactive)
     */
    @Transactional(readOnly = true)
    public List<HallResponseDTO> getAllHallsByTheater(Long theaterId) {
        return hallRepository.findByTheaterId(theaterId).stream()
                .map(HallMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Search halls by name
     */
    @Transactional(readOnly = true)
    public List<HallSummaryDTO> searchHalls(String name) {
        return hallRepository.searchByName(name).stream()
                .map(HallMapper::toSummaryDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all halls with pagination
     */
    @Transactional(readOnly = true)
    public Page<HallSummaryDTO> getAllHallsPaginated(Pageable pageable) {
        return hallRepository.findAll(pageable)
                .map(HallMapper::toSummaryDTO);
    }

    /**
     * Get halls by theater with pagination
     */
    @Transactional(readOnly = true)
    public Page<HallResponseDTO> getHallsByTheaterPaginated(Long theaterId, Pageable pageable) {
        return hallRepository.findByTheaterId(theaterId, pageable)
                .map(HallMapper::toResponseDTO);
    }

    /**
     * Get hall count by theater
     */
    @Transactional(readOnly = true)
    public long getHallCountByTheater(Long theaterId) {
        return hallRepository.countByTheaterIdAndIsActiveTrue(theaterId);
    }
}