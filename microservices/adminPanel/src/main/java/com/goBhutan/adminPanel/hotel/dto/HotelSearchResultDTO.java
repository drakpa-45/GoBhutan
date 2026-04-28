package com.goBhutan.adminPanel.hotel.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class HotelSearchResultDTO {

    private Long hotelId;
    private String name;
    private String city;
    private String district;
    private String thumbnailUrl;
    private List<String> imageUrls;

    private Integer starRating;
    private Double averageRating;      // e.g., 4.3
    private Integer reviewCount;

    private BigDecimal pricePerNight;  // lowest available room price
    private String currency;           // "BTN" / "USD"

    private Long totalBookings;        // popularity signal
    private Double distanceKm;         // null if no lat/lng provided

    private List<String> amenities;
    private Boolean isAvailable;       // null if no dates given
}