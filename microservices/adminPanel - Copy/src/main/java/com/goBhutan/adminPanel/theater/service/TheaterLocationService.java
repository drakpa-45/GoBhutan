package com.goBhutan.adminPanel.theater.service;

import com.goBhutan.adminPanel.theater.dto.theater.TheaterLocationDTO;
import com.goBhutan.adminPanel.theater.dto.theater.TheaterLocationResponseDTO;
import com.goBhutan.adminPanel.theater.entity.TheaterLocation;
import com.goBhutan.adminPanel.theater.mapper.TheaterLocationMapper;
import com.goBhutan.adminPanel.theater.repository.TheaterLocationRepository;
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
public class TheaterLocationService {

    private final TheaterLocationRepository locationRepository;

    public TheaterLocationResponseDTO createLocation(TheaterLocationDTO locationDTO) {
        log.info("Creating theater location: {}", locationDTO.getDzongkhag());

        // Check for duplicate
        if (locationRepository.existsByDzongkhagAndThromdoe(
                locationDTO.getDzongkhag(),
                locationDTO.getThromdoe())) {
            throw new IllegalArgumentException(
                    "Location already exists with Dzongkhag: " + locationDTO.getDzongkhag() +
                            " and Thromdoe: " + locationDTO.getThromdoe()
            );
        }

        TheaterLocation location = TheaterLocationMapper.toEntity(locationDTO);
        location.setCreatedAt(Instant.now());

        TheaterLocation savedLocation = locationRepository.save(location);
        log.info("Location created successfully with ID: {}", savedLocation.getId());

        return TheaterLocationMapper.toResponseDTO(savedLocation);
    }

    public TheaterLocationResponseDTO updateLocation(Long locationId, TheaterLocationDTO locationDTO) {
        log.info("Updating location with ID: {}", locationId);

        TheaterLocation location = locationRepository.findById(locationId)
                .orElseThrow(() -> new IllegalArgumentException("Location not found with ID: " + locationId));

        TheaterLocationMapper.updateEntityFromDTO(location, locationDTO);
        TheaterLocation updatedLocation = locationRepository.save(location);

        log.info("Location updated successfully: {}", locationId);
        return TheaterLocationMapper.toResponseDTO(updatedLocation);
    }

    public void deleteLocation(Long locationId) {
        log.info("Deleting location with ID: {}", locationId);

        TheaterLocation location = locationRepository.findById(locationId)
                .orElseThrow(() -> new IllegalArgumentException("Location not found with ID: " + locationId));

        if (location.getTheaters() != null && !location.getTheaters().isEmpty()) {
            throw new IllegalArgumentException(
                    "Cannot delete location with associated theaters. Please remove or reassign theaters first."
            );
        }

        locationRepository.delete(location);
        log.info("Location deleted successfully: {}", locationId);
    }

    @Transactional(readOnly = true)
    public TheaterLocationResponseDTO getLocationById(Long locationId) {
        TheaterLocation location = locationRepository.findById(locationId)
                .orElseThrow(() -> new IllegalArgumentException("Location not found with ID: " + locationId));

        return TheaterLocationMapper.toResponseDTO(location);
    }

    @Transactional(readOnly = true)
    public List<TheaterLocationResponseDTO> getAllLocations() {
        return locationRepository.findAll().stream()
                .map(TheaterLocationMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<TheaterLocationResponseDTO> getAllLocationsPaginated(Pageable pageable) {
        return locationRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(TheaterLocationMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public List<TheaterLocationResponseDTO> searchLocations(String keyword) {
        return locationRepository.searchLocations(keyword).stream()
                .map(TheaterLocationMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TheaterLocationResponseDTO> getLocationsByDzongkhag(String dzongkhag) {
        return locationRepository.findByDzongkhagContainingIgnoreCase(dzongkhag).stream()
                .map(TheaterLocationMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
}