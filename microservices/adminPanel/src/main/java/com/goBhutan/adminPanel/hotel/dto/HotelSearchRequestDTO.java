package com.goBhutan.adminPanel.hotel.dto;

import com.goBhutan.adminPanel.hotel.entity.Amenity;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class HotelSearchRequestDTO {

    private String keyword;

    private String city;
    private String state;
    private String country;

    private Integer minStars;
    private Integer maxStars;

    private BigDecimal minPrice;
    private BigDecimal maxPrice;

    private List<String> amenityNames;
    private List<Amenity.AmenityCategory> amenityCategories;

    private LocalDate checkIn;
    private LocalDate checkOut;
    private Integer guests;

    private SortOption sortBy = SortOption.NEWEST;
    private String sortDirection = "DESC";

    private int page = 0;
    private int size = 20;

    public enum SortOption {
        STAR_RATING,
        PRICE_LOW_HIGH,
        PRICE_HIGH_LOW,
        NEWEST
    }
}