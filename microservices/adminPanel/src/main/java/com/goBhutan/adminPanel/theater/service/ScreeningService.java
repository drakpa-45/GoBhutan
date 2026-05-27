package com.goBhutan.adminPanel.theater.service;

import com.goBhutan.adminPanel.theater.dto.screening.ScreeningDTO;
import com.goBhutan.adminPanel.theater.entity.Hall;
import com.goBhutan.adminPanel.theater.entity.Screening;
import com.goBhutan.adminPanel.theater.repository.HallRepository;
import com.goBhutan.adminPanel.theater.repository.ScreeningRepository;
import com.goBhutan.adminPanel.theater.repository.TheaterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ScreeningService {

    private final ScreeningRepository screeningRepository;
    private final TheaterRepository theaterRepository;
    private final HallRepository hallRepository;

    @Value("${file.upload.directory:/opt/uploads/movie}")
    private String uploadDirectory;

    @Transactional
    public ScreeningDTO createScreening(ScreeningDTO dto, MultipartFile posterImage) {
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

        if (posterImage != null && !posterImage.isEmpty()) {
            try {
                screening.setPosterImage(savePosterImage(posterImage, hall.getId()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        Screening saved = screeningRepository.save(screening);
        return mapToDTO(saved);
    }

    private String savePosterImage(MultipartFile posterImage, Long hallId) throws IOException {
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDirectory);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String originalFilename = posterImage.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

        // Create hall-specific subdirectory and save file
        Path posterDir = uploadPath.resolve(String.valueOf(hallId));
        if (!Files.exists(posterDir)) {
            Files.createDirectories(posterDir);
        }

        Files.copy(posterImage.getInputStream(), posterDir.resolve(uniqueFilename), StandardCopyOption.REPLACE_EXISTING);

        // Return relative path for database storage
        return uploadDirectory + "/" + hallId + "/" + uniqueFilename;
    }

    public ScreeningDTO getScreening(Long id) {
        Screening screening = screeningRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Screening not found"));
        return mapToDTO(screening);
    }

    public List<ScreeningDTO> getScreeningsByHall(Long hallId) {
        List<ScreeningDTO> screeningDTOList = screeningRepository.findByHallIdOrderByScreeningDateAscStartTimeAsc(hallId)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
        return screeningDTOList;
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
