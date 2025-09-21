package com.goBhutan.adminPanel.theater.dto;

import java.math.BigDecimal;
import java.util.List;

public class MovieCreateDTO {
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
    private String studioId;
    private List<String> categoryIds;

    // Constructors
    public MovieCreateDTO() {}

    // Getters and Setters
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

    public String getStudioId() { return studioId; }
    public void setStudioId(String studioId) { this.studioId = studioId; }

    public List<String> getCategoryIds() { return categoryIds; }
    public void setCategoryIds(List<String> categoryIds) { this.categoryIds = categoryIds; }
}
