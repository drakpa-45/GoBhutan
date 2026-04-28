package com.goBhutan.adminPanel.hotel.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class HotelSearchRequestDTO {

    // Full-text search on hotel name
    private String keyword;

    // Location filters
    private String city;
    private String district;     // Bhutan-specific (e.g., Paro, Thimphu)
    private Double latitude;
    private Double longitude;
    private Double radiusKm;     // Used with lat/lng for proximity search

    // Rating filter (e.g., minRating = 3.5 → returns 3.5+)
    private Double minRating;
    private Integer minStars;    // 1–5 star classification
    private Integer maxStars;

    // Price range
    private BigDecimal minPrice;
    private BigDecimal maxPrice;

    // Amenities (e.g., ["WIFI", "POOL", "SPA"])
    private List<String> amenities;

    // Availability (optional — for room-aware search)
    private java.time.LocalDate checkIn;
    private java.time.LocalDate checkOut;
    private Integer guests;

    // Sorting
    private SortOption sortBy = SortOption.POPULARITY;
    private String sortDirection = "DESC"; // ASC | DESC

    // Pagination
    private int page = 0;
    private int size = 20;

    public enum SortOption {
        POPULARITY,       // by total booking count
        RATING,           // by average review rating
        PRICE_LOW_HIGH,
        PRICE_HIGH_LOW,
        DISTANCE,         // requires lat/lng
        NEWEST
    }
}