package com.goBhutan.adminPanel.theater.dto;

import java.math.BigDecimal;
import java.util.List;

public class MovieResponseDTO {
    private String id;
    private String title;
    private String description;
    private Integer durationMinutes;
    private String language;
    private String director;
    private String cast;
    private String releaseDate;
    private String posterUrl;
    private String trailerUrl;
    private String rating;
    private BigDecimal imdbRating;
    private String status;
    private MovieStudioDTO studio;
    private List<MovieCategoryDTO> categories;
    private Boolean isActive;
    private String createdAt;

    // Constructors
    public MovieResponseDTO() {}

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getDirector() { return director; }
    public void setDirector(String director) { this.director = director; }

    public String getCast() { return cast; }
    public void setCast(String cast) { this.cast = cast; }

    public String getReleaseDate() { return releaseDate; }
    public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }

    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }

    public String getTrailerUrl() { return trailerUrl; }
    public void setTrailerUrl(String trailerUrl) { this.trailerUrl = trailerUrl; }

    public String getRating() { return rating; }
    public void setRating(String rating) { this.rating = rating; }

    public BigDecimal getImdbRating() { return imdbRating; }
    public void setImdbRating(BigDecimal imdbRating) { this.imdbRating = imdbRating; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public MovieStudioDTO getStudio() { return studio; }
    public void setStudio(MovieStudioDTO studio) { this.studio = studio; }

    public List<MovieCategoryDTO> getCategories() { return categories; }
    public void setCategories(List<MovieCategoryDTO> categories) { this.categories = categories; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}