package com.goBhutan.adminPanel.theater.service;

import com.goBhutan.adminPanel.common.entity.AppUser;
import com.goBhutan.adminPanel.theater.dto.*;
import com.goBhutan.adminPanel.theater.entity.*;
import com.goBhutan.adminPanel.theater.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TheaterService {

    private final TheaterRepository theaterRepository;
    private final TheaterLocationRepository theaterLocationRepository;
    private final HallRepository hallRepository;
    private final SeatRepository seatRepository;
    private final ScreeningRepository screeningRepository;
    private final com.goBhutan.adminPanel.common.repository.AppUserRepository appUserRepository;

    public TheaterService(TheaterRepository theaterRepository,
                          TheaterLocationRepository theaterLocationRepository,
                          HallRepository hallRepository,
                          SeatRepository seatRepository,
                          ScreeningRepository screeningRepository,
                          com.goBhutan.adminPanel.common.repository.AppUserRepository appUserRepository) {
        this.theaterRepository = theaterRepository;
        this.theaterLocationRepository = theaterLocationRepository;
        this.hallRepository = hallRepository;
        this.seatRepository = seatRepository;
        this.screeningRepository = screeningRepository;
        this.appUserRepository = appUserRepository;
    }

    public Page<TheaterDTO> getAllTheaters(Pageable pageable) {
        return theaterRepository.findAllByIsActiveTrueOrderByCreatedAtDesc(pageable)
                .map(this::convertToDTO);
    }

    public List<TheaterDTO> getAllTheatersList() {
        return theaterRepository.findAllByIsActiveTrueOrderByNameAsc()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Page<TheaterDTO> getTheatersByOwner(String adminUserId, Pageable pageable) {
        return theaterRepository.findByAdminUserIdAndIsActiveTrueOrderByCreatedAtDesc(adminUserId, pageable)
                .map(this::convertToDTO);
    }

    public List<TheaterDTO> getTheatersByOwnerList(String adminUserId) {
        return theaterRepository.findByAdminUserIdAndIsActiveTrueOrderByNameAsc(adminUserId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Page<TheaterDTO> getTheatersByLocation(String locationId, Pageable pageable) {
        return theaterRepository.findByLocationIdAndIsActiveTrueOrderByCreatedAtDesc(locationId, pageable)
                .map(this::convertToDTO);
    }

    public List<TheaterDTO> getTheatersByLocationList(String locationId) {
        return theaterRepository.findByLocationIdAndIsActiveTrueOrderByNameAsc(locationId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Page<TheaterDTO> getTheatersByDzongkhag(String dzongkhag, Pageable pageable) {
        return theaterRepository.findByLocationDzongkhagContainingIgnoreCaseAndIsActiveTrueOrderByCreatedAtDesc(
                        dzongkhag, pageable)
                .map(this::convertToDTO);
    }

    public Page<TheaterDTO> searchTheaters(String search, Pageable pageable) {
        return theaterRepository.findByNameContainingIgnoreCaseAndIsActiveTrueOrderByCreatedAtDesc(
                        search, pageable)
                .map(this::convertToDTO);
    }

    public TheaterDTO getTheaterById(String id) {
        Theater theater = theaterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Theater not found with id: " + id));
        return convertToDTO(theater);
    }

    public TheaterDTO getTheaterWithHalls(String id) {
        Theater theater = theaterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Theater not found with id: " + id));

        TheaterDTO dto = convertToDTO(theater);

        List<HallDTO> halls = hallRepository.findByTheaterId(id)
                .stream()
                .map(this::convertHallToDTO)
                .collect(Collectors.toList());
        dto.setHalls(halls);

        return dto;
    }

    public TheaterStatsDTO getTheaterStatistics(String id) {
        Theater theater = theaterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Theater not found with id: " + id));

        Long totalHalls = hallRepository.countByTheaterIdAndIsActive(id, true);
        Long totalSeats = seatRepository.countByTheaterIdAndIsActive(id);
        Long activeScreenings = screeningRepository.countByTheaterIdAndIsActive(id);

        TheaterStatsDTO stats = new TheaterStatsDTO();
        stats.setTheaterId(id);
        stats.setTheaterName(theater.getName());
        stats.setTotalHalls(totalHalls);
        stats.setTotalSeats(totalSeats);
        stats.setActiveScreenings(activeScreenings);

        return stats;
    }

    public TheaterDTO createTheater(TheaterCreateDTO createDTO, String adminUserId) {
        if (theaterRepository.existsByNameIgnoreCaseAndLocationId(createDTO.getName(), createDTO.getLocationId())) {
            throw new RuntimeException("Theater with name '" + createDTO.getName() + "' already exists in this location");
        }

        TheaterLocation location = theaterLocationRepository.findById(createDTO.getLocationId())
                .orElseThrow(() -> new RuntimeException("Location not found with id: " + createDTO.getLocationId()));

        AppUser owner = appUserRepository.findByKeycloakId(adminUserId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + adminUserId));

        Theater theater = new Theater();
        theater.setName(createDTO.getName());
        theater.setDescription(createDTO.getDescription());
        theater.setLocation(location);
        theater.setAdminUserId(adminUserId);

        Theater savedTheater = theaterRepository.save(theater);
        return convertToDTO(savedTheater);
    }

    public TheaterDTO updateTheater(String id, TheaterCreateDTO updateDTO) {
        Theater theater = theaterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Theater not found with id: " + id));

        if (!theater.getName().equalsIgnoreCase(updateDTO.getName()) &&
                theaterRepository.existsByNameIgnoreCaseAndLocationId(updateDTO.getName(), updateDTO.getLocationId())) {
            throw new RuntimeException("Theater with name '" + updateDTO.getName() + "' already exists in this location");
        }

        TheaterLocation location = theaterLocationRepository.findById(updateDTO.getLocationId())
                .orElseThrow(() -> new RuntimeException("Location not found with id: " + updateDTO.getLocationId()));

        theater.setName(updateDTO.getName());
        theater.setDescription(updateDTO.getDescription());
        theater.setLocation(location);

        Theater updatedTheater = theaterRepository.save(theater);
        return convertToDTO(updatedTheater);
    }

    public TheaterDTO toggleTheaterActive(String id) {
        Theater theater = theaterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Theater not found with id: " + id));

        theater.setIsActive(!theater.getIsActive());
        Theater updatedTheater = theaterRepository.save(theater);
        return convertToDTO(updatedTheater);
    }

    public void deleteTheater(String id) {
        Theater theater = theaterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Theater not found with id: " + id));

        theater.setIsActive(false);
        theaterRepository.save(theater);
    }

    public TheaterDTO transferOwnership(String theaterId, String newOwnerId) {
        Theater theater = theaterRepository.findById(theaterId)
                .orElseThrow(() -> new RuntimeException("Theater not found with id: " + theaterId));

        AppUser newOwner = appUserRepository.findByKeycloakId(newOwnerId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + newOwnerId));

        theater.setAdminUserId(newOwnerId);
        Theater updatedTheater = theaterRepository.save(theater);
        return convertToDTO(updatedTheater);
    }

    private TheaterDTO convertToDTO(Theater theater) {
        TheaterDTO dto = new TheaterDTO();
        dto.setId(theater.getId());
        dto.setName(theater.getName());
        dto.setDescription(theater.getDescription());
        dto.setOwnerId(theater.getAdminUserId());
        dto.setIsActive(theater.getIsActive());
        dto.setCreatedAt(theater.getCreatedAt().toString());

        if (theater.getLocation() != null) {
            dto.setLocation(convertLocationToDTO(theater.getLocation()));
        }

        return dto;
    }

    private TheaterLocationDTO convertLocationToDTO(TheaterLocation location) {
        TheaterLocationDTO dto = new TheaterLocationDTO();
        dto.setId(location.getId());
        dto.setDzongkhag(location.getDzongkhag());
        dto.setThromdoe(location.getThromdoe());
        dto.setTown(location.getTown());
        dto.setAddress(location.getAddress());
        dto.setCreatedAt(location.getCreatedAt().toString());
        return dto;
    }

    private HallDTO convertHallToDTO(Hall hall) {
        HallDTO dto = new HallDTO();
        dto.setId(hall.getId());
        dto.setName(hall.getName());
        dto.setTotalSeats(hall.getTotalSeats());
        dto.setTheaterId(hall.getTheater().getId());
        dto.setTheaterName(hall.getTheater().getName());
        dto.setIsActive(hall.getIsActive());
        dto.setCreatedAt(hall.getCreatedAt().toString());
        return dto;
    }
}
