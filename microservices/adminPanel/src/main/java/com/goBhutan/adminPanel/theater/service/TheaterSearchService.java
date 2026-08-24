package com.goBhutan.adminPanel.theater.service;

import com.goBhutan.adminPanel.theater.dto.screening.MovieScreeningResponseDTO;
import com.goBhutan.adminPanel.theater.entity.Hall;
import com.goBhutan.adminPanel.theater.entity.Screening;
import com.goBhutan.adminPanel.theater.entity.Theater;
import com.goBhutan.adminPanel.theater.entity.TheaterLocation;
import com.goBhutan.adminPanel.theater.repository.ScreeningSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TheaterSearchService {

    private final ScreeningSearchRepository screeningSearchRepository;

    public List<MovieScreeningResponseDTO> findTheatersScreeningMovie(
            String movieName, String dzongkhag, LocalDate date) {

        LocalDate fromDate = (date != null) ? date : LocalDate.now();

        log.info("Searching screenings — movie: '{}', dzongkhag: '{}', from: {}",
                movieName, dzongkhag, fromDate);

        List<Screening> screenings = (dzongkhag != null && !dzongkhag.isBlank())
                ? screeningSearchRepository
                .findActiveScreeningsByMovieNameAndDzongkhag(movieName, fromDate, dzongkhag)
                : screeningSearchRepository
                .findActiveScreeningsByMovieName(movieName, fromDate);

        if (screenings.isEmpty()) {
            return Collections.emptyList();
        }

        // Group: theaterId → hallId → List<Screening>
        Map<Long, Map<Long, List<Screening>>> grouped = screenings.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getHall().getTheater().getId(),
                        LinkedHashMap::new,
                        Collectors.groupingBy(
                                s -> s.getHall().getId(),
                                LinkedHashMap::new,
                                Collectors.toList()
                        )
                ));

        return grouped.entrySet().stream().map(theaterEntry -> {

            Theater theater = screenings.stream()
                    .filter(s -> s.getHall().getTheater().getId().equals(theaterEntry.getKey()))
                    .findFirst().get().getHall().getTheater();

            TheaterLocation loc = theater.getLocation();

            List<MovieScreeningResponseDTO.HallScreeningDTO> halls =
                    theaterEntry.getValue().entrySet().stream().map(hallEntry -> {

                        Hall hall = hallEntry.getValue().get(0).getHall();

                        List<MovieScreeningResponseDTO.ShowTimeDTO> showTimes =
                                hallEntry.getValue().stream()
                                        .map(s -> MovieScreeningResponseDTO.ShowTimeDTO.builder()
                                                .screeningId(s.getId())
                                                .screeningDate(s.getScreeningDate())
                                                .startTime(s.getStartTime())
                                                .trailerLink(s.getTrailerLink())
                                                .build())
                                        .collect(Collectors.toList());

                        return MovieScreeningResponseDTO.HallScreeningDTO.builder()
                                .hallId(hall.getId())
                                .hallName(hall.getName())
                                .totalSeats(hall.getTotalSeats())
                                .showTimes(showTimes)
                                .build();

                    }).collect(Collectors.toList());

            return MovieScreeningResponseDTO.builder()
                    .theaterId(theater.getId())
                    .theaterName(theater.getName())
                    .theaterDescription(theater.getDescription())
                    .locationId(loc.getId())
                    .dzongkhag(loc.getDzongkhag())
                    .thromdoe(loc.getThromdoe())
                    .address(loc.getAddress())
                    .displayLocation(loc.getName()) // "Thimphu, Thimphu Thromde"
                    .halls(halls)
                    .build();

        }).collect(Collectors.toList());
    }
}