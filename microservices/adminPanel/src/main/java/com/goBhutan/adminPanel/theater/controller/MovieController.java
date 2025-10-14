package com.goBhutan.adminPanel.theater.controller;

import com.goBhutan.adminPanel.theater.dto.MovieCreateDTO;
import com.goBhutan.adminPanel.theater.dto.MovieResponseDTO;
import com.goBhutan.adminPanel.theater.entity.Movie;
import com.goBhutan.adminPanel.theater.service.MovieService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/movies")
@PreAuthorize("hasRole('ADMIN') or hasRole('THEATER_OWNER')")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping
    public ResponseEntity<Page<MovieResponseDTO>> getAllMovies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String studioId,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String search) {

        Pageable pageable = PageRequest.of(page, size);
        Page<MovieResponseDTO> movies;

        if (search != null && !search.trim().isEmpty()) {
            movies = movieService.searchMovies(search, pageable);
        } else if (status != null) {
            Movie.MovieStatus movieStatus = Movie.MovieStatus.valueOf(status.toUpperCase());
            movies = movieService.getMoviesByStatus(movieStatus, pageable);
        } else if (studioId != null) {
            movies = movieService.getMoviesByStudio(studioId, pageable);
        } else if (categoryId != null) {
            movies = movieService.getMoviesByCategory(categoryId, pageable);
        } else {
            movies = movieService.getAllMovies(pageable);
        }

        return ResponseEntity.ok(movies);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovieResponseDTO> getMovieById(@PathVariable String id) {
        MovieResponseDTO movie = movieService.getMovieById(id);
        return ResponseEntity.ok(movie);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MovieResponseDTO> createMovie(@RequestBody @Valid MovieCreateDTO createDTO) {
        MovieResponseDTO createdMovie = movieService.createMovie(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdMovie);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MovieResponseDTO> updateMovie(@PathVariable String id,
                                                        @RequestBody @Valid MovieCreateDTO updateDTO) {
        MovieResponseDTO updatedMovie = movieService.updateMovie(id, updateDTO);
        return ResponseEntity.ok(updatedMovie);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MovieResponseDTO> updateMovieStatus(@PathVariable String id,
                                                              @RequestParam String status) {
        Movie.MovieStatus movieStatus = Movie.MovieStatus.valueOf(status.toUpperCase());
        MovieResponseDTO updatedMovie = movieService.updateMovieStatus(id, movieStatus);
        return ResponseEntity.ok(updatedMovie);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMovie(@PathVariable String id) {
        movieService.deleteMovie(id);
        return ResponseEntity.noContent().build();
    }
}