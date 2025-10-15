package com.goBhutan.adminPanel.theater.service;

import com.goBhutan.adminPanel.theater.dto.*;
import com.goBhutan.adminPanel.theater.entity.*;
import com.goBhutan.adminPanel.theater.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class MovieService {

    private final MovieRepository movieRepository;
    private final MovieStudioRepository movieStudioRepository;
    private final MovieCategoryRepository movieCategoryRepository;

    public MovieService(MovieRepository movieRepository,
                        MovieStudioRepository movieStudioRepository,
                        MovieCategoryRepository movieCategoryRepository) {
        this.movieRepository = movieRepository;
        this.movieStudioRepository = movieStudioRepository;
        this.movieCategoryRepository = movieCategoryRepository;
    }

    public Page<MovieResponseDTO> getAllMovies(Pageable pageable) {
        return movieRepository.findAllByIsActiveTrueOrderByCreatedAtDesc(pageable)
                .map(this::convertToResponseDTO);
    }

    public Page<MovieResponseDTO> getMoviesByStatus(Movie.MovieStatus status, Pageable pageable) {
        return movieRepository.findByStatusAndIsActiveTrueOrderByCreatedAtDesc(status, pageable)
                .map(this::convertToResponseDTO);
    }

    public Page<MovieResponseDTO> getMoviesByStudio(String studioId, Pageable pageable) {
        return movieRepository.findByStudioIdAndIsActiveTrueOrderByCreatedAtDesc(studioId, pageable)
                .map(this::convertToResponseDTO);
    }

    public Page<MovieResponseDTO> getMoviesByCategory(String categoryId, Pageable pageable) {
        return movieRepository.findByCategoryIdAndIsActiveTrue(categoryId, pageable)
                .map(this::convertToResponseDTO);
    }

    public Page<MovieResponseDTO> searchMovies(String title, Pageable pageable) {
        return movieRepository.findByTitleContainingIgnoreCaseAndIsActiveTrue(title, pageable)
                .map(this::convertToResponseDTO);
    }

    public MovieResponseDTO getMovieById(String id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + id));
        return convertToResponseDTO(movie);
    }

    public MovieResponseDTO createMovie(MovieCreateDTO createDTO) {
        Movie movie = new Movie();
        updateMovieFromDTO(movie, createDTO);

        Movie savedMovie = movieRepository.save(movie);
        return convertToResponseDTO(savedMovie);
    }

    public MovieResponseDTO updateMovie(String id, MovieCreateDTO updateDTO) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + id));

        updateMovieFromDTO(movie, updateDTO);
        Movie updatedMovie = movieRepository.save(movie);
        return convertToResponseDTO(updatedMovie);
    }

    public MovieResponseDTO updateMovieStatus(String id, Movie.MovieStatus status) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + id));

        movie.setStatus(status);
        Movie updatedMovie = movieRepository.save(movie);
        return convertToResponseDTO(updatedMovie);
    }

    public void deleteMovie(String id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + id));

        movie.setIsActive(false);
        movieRepository.save(movie);
    }

    private void updateMovieFromDTO(Movie movie, MovieCreateDTO dto) {
        movie.setTitle(dto.getTitle());
        movie.setDescription(dto.getDescription());
        movie.setDurationMinutes(dto.getDurationMinutes());
        movie.setLanguage(dto.getLanguage());
        movie.setDirector(dto.getDirector());
        movie.setCast(dto.getCast());
        movie.setPosterUrl(dto.getPosterUrl());
        movie.setTrailerUrl(dto.getTrailerUrl());
        movie.setRating(dto.getRating());
        movie.setImdbRating(dto.getImdbRating());

        if (dto.getReleaseDate() != null) {
            movie.setReleaseDate(LocalDate.parse(dto.getReleaseDate()));
        }

        if (dto.getStudioId() != null) {
            MovieStudio studio = movieStudioRepository.findById(dto.getStudioId())
                    .orElseThrow(() -> new RuntimeException("Studio not found with id: " + dto.getStudioId()));
            movie.setStudio(studio);
        }

        if (dto.getCategoryIds() != null && !dto.getCategoryIds().isEmpty()) {
            List<MovieCategory> categories = movieCategoryRepository.findAllById(dto.getCategoryIds());
            if (categories.size() != dto.getCategoryIds().size()) {
                throw new RuntimeException("Some categories not found");
            }
            movie.setCategories(categories);
        }
    }

    private MovieResponseDTO convertToResponseDTO(Movie movie) {
        MovieResponseDTO dto = new MovieResponseDTO();
        dto.setId(movie.getId());
        dto.setTitle(movie.getTitle());
        dto.setDescription(movie.getDescription());
        dto.setDurationMinutes(movie.getDurationMinutes());
        dto.setLanguage(movie.getLanguage());
        dto.setDirector(movie.getDirector());
        dto.setCast(movie.getCast());
        dto.setReleaseDate(movie.getReleaseDate() != null ? movie.getReleaseDate().toString() : null);
        dto.setPosterUrl(movie.getPosterUrl());
        dto.setTrailerUrl(movie.getTrailerUrl());
        dto.setRating(movie.getRating());
        dto.setImdbRating(movie.getImdbRating());
        dto.setStatus(movie.getStatus().name());
        dto.setIsActive(movie.getIsActive());
        dto.setCreatedAt(movie.getCreatedAt().toString());

        if (movie.getStudio() != null) {
            dto.setStudio(convertStudioToDTO(movie.getStudio()));
        }

        if (movie.getCategories() != null) {
            dto.setCategories(movie.getCategories().stream()
                    .map(this::convertCategoryToDTO)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    private MovieStudioDTO convertStudioToDTO(MovieStudio studio) {
        MovieStudioDTO dto = new MovieStudioDTO();
        dto.setId(studio.getId());
        dto.setName(studio.getName());
        dto.setDescription(studio.getDescription());
        dto.setCountry(studio.getCountry());
        dto.setWebsite(studio.getWebsite());
        dto.setIsActive(studio.getIsActive());
        dto.setCreatedAt(studio.getCreatedAt().toString());
        return dto;
    }

    private MovieCategoryDTO convertCategoryToDTO(MovieCategory category) {
        MovieCategoryDTO dto = new MovieCategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setIsActive(category.getIsActive());
        dto.setCreatedAt(category.getCreatedAt().toString());
        return dto;
    }
}
