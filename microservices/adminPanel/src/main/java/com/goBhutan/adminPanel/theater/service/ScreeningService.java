package com.goBhutan.adminPanel.theater.service;

import com.goBhutan.adminPanel.theater.dto.screening.ScreeningDTO;
import com.goBhutan.adminPanel.theater.entity.Hall;
import com.goBhutan.adminPanel.theater.entity.Screening;
import com.goBhutan.adminPanel.theater.entity.Theater;
import com.goBhutan.adminPanel.theater.repository.HallRepository;
import com.goBhutan.adminPanel.theater.repository.ScreeningRepository;
import com.goBhutan.adminPanel.theater.repository.TheaterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ScreeningService {

    private final ScreeningRepository screeningRepository;
    private final TheaterRepository theaterRepository;
    private final HallRepository hallRepository;

    public ScreeningDTO createScreening(ScreeningDTO dto) {
       /* Theater theater = theaterRepository.findById(dto.getTheaterId())
                .orElseThrow(() -> new IllegalArgumentException("Theater not found"));*/
        Hall hall = hallRepository.findById(dto.getHallId())
                .orElseThrow(() -> new IllegalArgumentException("Hall not found"));

        Screening screening = new Screening();
        screening.setMovieName(dto.getMovieName());
        screening.setScreeningDate(dto.getScreeningDate());
        screening.setStartTime(dto.getStartTime());
        screening.setTrailerLink(dto.getTrailerLink());
       // screening.setTheater(theater);
        screening.setHall(hall);
        screening.setIsActive(true);

        Screening saved = screeningRepository.save(screening);
        return mapToDTO(saved);
    }

    public ScreeningDTO getScreening(Long id) {
        Screening screening = screeningRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Screening not found"));
        return mapToDTO(screening);
    }

    public List<ScreeningDTO> getScreeningsByHall(Long hallId) {
        return screeningRepository.findByHallIdOrderByScreeningDateAscStartTimeAsc(hallId)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public ScreeningDTO updateScreening(Long id, ScreeningDTO dto) {
        Screening screening = screeningRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Screening not found"));

        screening.setMovieName(dto.getMovieName());
        screening.setScreeningDate(dto.getScreeningDate());
        screening.setStartTime(dto.getStartTime());
        screening.setTrailerLink(dto.getTrailerLink());
        screening.setIsActive(dto.getIsActive());

        return mapToDTO(screeningRepository.save(screening));
    }

    public void deleteScreening(Long id) {
        screeningRepository.deleteById(id);
    }

    private ScreeningDTO mapToDTO(Screening screening) {
        ScreeningDTO dto = new ScreeningDTO();
        dto.setId(screening.getId());
        dto.setMovieName(screening.getMovieName());
        dto.setScreeningDate(screening.getScreeningDate());
        dto.setStartTime(screening.getStartTime());
        dto.setTrailerLink(screening.getTrailerLink());
    //    dto.setTheaterId(screening.getTheater().getId());
       // dto.setTheaterName(screening.getTheater().getName());
        dto.setHallId(screening.getHall().getId());
        dto.setHallName(screening.getHall().getName());
        dto.setIsActive(screening.getIsActive());
        return dto;
    }
}
