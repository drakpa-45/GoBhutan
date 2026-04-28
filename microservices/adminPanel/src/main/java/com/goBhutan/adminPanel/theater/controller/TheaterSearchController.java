package com.goBhutan.adminPanel.theater.controller;

import com.goBhutan.adminPanel.common.dto.ApiResponse;
import com.goBhutan.adminPanel.theater.dto.screening.MovieScreeningResponseDTO;
import com.goBhutan.adminPanel.theater.service.TheaterSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Public Search", description = "Public search APIs for mobile app — no auth required")
public class TheaterSearchController {

    private final TheaterSearchService theaterSearchService;

    /**
     * GET /api/search/now-screening?movieName=Avatar
     * GET /api/search/now-screening?movieName=Avatar&dzongkhag=Thimphu
     * GET /api/search/now-screening?movieName=Avatar&dzongkhag=Thimphu&date=2026-04-28
     */
    @GetMapping("/now-screening")
    @Operation(summary = "Find theaters & halls screening a movie by name")
    public ResponseEntity<ApiResponse<List<MovieScreeningResponseDTO>>> findTheatersScreeningMovie(
            @RequestParam String movieName,
            @RequestParam(required = false) String dzongkhag,  // Bhutan district filter
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        try {
            if (movieName.isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Movie name cannot be empty"));
            }

            List<MovieScreeningResponseDTO> results =
                    theaterSearchService.findTheatersScreeningMovie(movieName, dzongkhag, date);

            if (results.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.success(
                        "No theaters are currently screening \"" + movieName + "\"", results));
            }

            return ResponseEntity.ok(ApiResponse.success(
                    results.size() + " theater(s) screening \"" + movieName + "\"", results));

        } catch (Exception e) {
            log.error("Error searching screenings for '{}': {}", movieName, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch screenings: " + e.getMessage()));
        }
    }
}