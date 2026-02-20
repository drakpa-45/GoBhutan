package com.goBhutan.adminPanel.theater.service;

import com.goBhutan.adminPanel.theater.dto.theater.TheaterDTO;
import com.goBhutan.adminPanel.theater.dto.theater.TheaterResponseDTO;
import com.goBhutan.adminPanel.theater.dto.theater.TheaterSummaryDTO;
import com.goBhutan.adminPanel.theater.dto.theater.TheaterUpdateDTO;
import com.goBhutan.adminPanel.theater.entity.Theater;
import com.goBhutan.adminPanel.theater.entity.TheaterLocation;
import com.goBhutan.adminPanel.theater.mapper.TheaterMapper;
import com.goBhutan.adminPanel.theater.repository.TheaterLocationRepository;
import com.goBhutan.adminPanel.theater.repository.TheaterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TheaterService {

    private final TheaterRepository theaterRepository;
    private final TheaterLocationRepository locationRepository;

    /**
     * Create a new theater
     */
    public TheaterResponseDTO createTheater(TheaterDTO theaterDTO) {
        log.info("Creating theater: {}", theaterDTO.getName());

        // Check if theater with same name exists in the same location
        if (theaterRepository.existsByNameAndLocationId(theaterDTO.getName(), theaterDTO.getLocationId())) {
            throw new IllegalArgumentException(
                    "Theater with name '" + theaterDTO.getName() + "' already exists in this location"
            );
        }

        // Fetch location
        TheaterLocation location = locationRepository.findById(theaterDTO.getLocationId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Location not found with ID: " + theaterDTO.getLocationId()
                ));

        // Create theater entity
        Theater theater = TheaterMapper.toEntity(theaterDTO);
        theater.setLocation(location);
        theater.setIsActive(true);
        theater.setCreatedAt(Instant.now());
        theater.setUpdatedAt(Instant.now());

        // Save theater
        Theater savedTheater = theaterRepository.save(theater);
        log.info("Theater created successfully with ID: {}", savedTheater.getId());

        return TheaterMapper.toResponseDTO(savedTheater);
    }

    /**
     * Update an existing theater
     */
    public TheaterResponseDTO updateTheater(Long theaterId, TheaterUpdateDTO updateDTO) {
        log.info("Updating theater with ID: {}", theaterId);

        // Find existing theater
        Theater theater = theaterRepository.findById(theaterId)
                .orElseThrow(() -> new IllegalArgumentException("Theater not found with ID: " + theaterId));

        // Check for duplicate name if name is being updated
        if (updateDTO.getName() != null && !updateDTO.getName().equals(theater.getName())) {
            Long locationId = updateDTO.getLocationId() != null ?
                    updateDTO.getLocationId() : theater.getLocation().getId();

            if (theaterRepository.existsByNameAndLocationId(updateDTO.getName(), locationId)) {
                throw new IllegalArgumentException(
                        "Theater with name '" + updateDTO.getName() + "' already exists in this location"
                );
            }
        }

        // Update location if provided
        if (updateDTO.getLocationId() != null && !updateDTO.getLocationId().equals(theater.getLocation().getId())) {
            TheaterLocation newLocation = locationRepository.findById(updateDTO.getLocationId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Location not found with ID: " + updateDTO.getLocationId()
                    ));
            theater.setLocation(newLocation);
        }

        // Update other fields
        TheaterMapper.updateEntityFromDTO(theater, updateDTO);
        theater.setUpdatedAt(Instant.now());

        // Save updated theater
        Theater updatedTheater = theaterRepository.save(theater);
        log.info("Theater updated successfully: {}", theaterId);

        return TheaterMapper.toResponseDTO(updatedTheater);
    }

    /**
     * Soft delete a theater
     */
    public void softDeleteTheater(Long theaterId) {
        log.info("Soft deleting theater with ID: {}", theaterId);

        Theater theater = theaterRepository.findById(theaterId)
                .orElseThrow(() -> new IllegalArgumentException("Theater not found with ID: " + theaterId));

        theater.setIsActive(false);
        theater.setUpdatedAt(Instant.now());

        theaterRepository.save(theater);
        log.info("Theater soft deleted successfully: {}", theaterId);
    }

    /**
     * Get theater by ID
     */
    @Transactional(readOnly = true)
    public TheaterResponseDTO getTheaterById(Long theaterId) {
        Theater theater = theaterRepository.findById(theaterId)
                .orElseThrow(() -> new IllegalArgumentException("Theater not found with ID: " + theaterId));

        return TheaterMapper.toResponseDTO(theater);
    }

    /**
     * Get all active theaters
     */
    @Transactional(readOnly = true)
    public List<TheaterSummaryDTO> getAllActiveTheaters() {
        return theaterRepository.findByIsActiveTrue().stream()
                .map(TheaterMapper::toSummaryDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all active theaters by user id
     */
    @Transactional(readOnly = true)
    public List<TheaterSummaryDTO> getAllTheatersByUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId;

        if (principal instanceof Jwt jwt) {
            userId = jwt.getSubject(); // Keycloak "sub" claim
        } else if (principal instanceof String str) {
            userId = str; // fallback if principal is String
        } else {
            throw new RuntimeException("Unsupported principal type: " + principal.getClass());
        }

        return theaterRepository.findByAdminUserId(userId).stream()
                .map(TheaterMapper::toSummaryDTO)
                .collect(Collectors.toList());
    }



    /**
     * Get theaters by location
     */
    @Transactional(readOnly = true)
    public List<TheaterSummaryDTO> getTheatersByLocation(Long locationId) {
        return theaterRepository.findByLocationIdAndIsActiveTrue(locationId).stream()
                .map(TheaterMapper::toSummaryDTO)
                .collect(Collectors.toList());
    }

    /**
     * Search theaters by name
     */
    @Transactional(readOnly = true)
    public List<TheaterSummaryDTO> searchTheaters(String name) {
        return theaterRepository.searchByName(name).stream()
                .map(TheaterMapper::toSummaryDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all theaters with pagination
     */
    @Transactional(readOnly = true)
    public Page<TheaterSummaryDTO> getAllTheatersPaginated(Pageable pageable) {
        return theaterRepository.findAll(pageable)
                .map(TheaterMapper::toSummaryDTO);
    }

    /**
     * Restore soft deleted theater
     */
    public void restoreTheater(Long theaterId) {
        log.info("Restoring theater with ID: {}", theaterId);

        Theater theater = theaterRepository.findById(theaterId)
                .orElseThrow(() -> new IllegalArgumentException("Theater not found with ID: " + theaterId));

        theater.setIsActive(true);
        theater.setUpdatedAt(Instant.now());

        theaterRepository.save(theater);
        log.info("Theater restored successfully: {}", theaterId);
    }
}