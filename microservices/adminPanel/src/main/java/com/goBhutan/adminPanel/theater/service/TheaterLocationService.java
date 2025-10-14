package com.goBhutan.adminPanel.theater.service;

import com.goBhutan.adminPanel.theater.dto.TheaterLocationCreateDTO;
import com.goBhutan.adminPanel.theater.dto.TheaterLocationDTO;
import com.goBhutan.adminPanel.theater.entity.TheaterLocation;
import com.goBhutan.adminPanel.theater.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
@Service
public class TheaterLocationService {

    private final TheaterLocationRepository theaterLocationRepository;

    public TheaterLocationService(TheaterLocationRepository theaterLocationRepository) {
        this.theaterLocationRepository = theaterLocationRepository;
    }

    public Page<TheaterLocationDTO> getAllLocations(Pageable pageable) {
        return theaterLocationRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::convertToDTO);
    }

    public List<TheaterLocationDTO> getLocationsByDzongkhag(String dzongkhag) {
        return theaterLocationRepository.findByDzongkhagContainingIgnoreCase(dzongkhag)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public TheaterLocationDTO getLocationById(String id) {
        TheaterLocation location = theaterLocationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Location not found with id: " + id));
        return convertToDTO(location);
    }

    public TheaterLocationDTO createLocation(TheaterLocationCreateDTO createDTO) {
        if (theaterLocationRepository.existsByDzongkhagAndThromdoeAndTown(
                createDTO.getDzongkhag(),
                createDTO.getThromdoe(),
                createDTO.getTown())) {
            throw new RuntimeException("Location already exists");
        }

        TheaterLocation location = new TheaterLocation();
        location.setDzongkhag(createDTO.getDzongkhag());
        location.setThromdoe(createDTO.getThromdoe());
        location.setTown(createDTO.getTown());
        location.setAddress(createDTO.getAddress());

        TheaterLocation savedLocation = theaterLocationRepository.save(location);
        return convertToDTO(savedLocation);
    }

    public TheaterLocationDTO updateLocation(String id, TheaterLocationCreateDTO updateDTO) {
        TheaterLocation location = theaterLocationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Location not found with id: " + id));

        location.setDzongkhag(updateDTO.getDzongkhag());
        location.setThromdoe(updateDTO.getThromdoe());
        location.setTown(updateDTO.getTown());
        location.setAddress(updateDTO.getAddress());

        TheaterLocation updatedLocation = theaterLocationRepository.save(location);
        return convertToDTO(updatedLocation);
    }

    public void deleteLocation(String id) {
        theaterLocationRepository.deleteById(id);
    }

    private TheaterLocationDTO convertToDTO(TheaterLocation location) {
        TheaterLocationDTO dto = new TheaterLocationDTO();
        dto.setId(location.getId());
        dto.setDzongkhag(location.getDzongkhag());
        dto.setThromdoe(location.getThromdoe());
        dto.setTown(location.getTown());
        dto.setAddress(location.getAddress());
        dto.setCreatedAt(location.getCreatedAt().toString());
        return dto;
    }
}
