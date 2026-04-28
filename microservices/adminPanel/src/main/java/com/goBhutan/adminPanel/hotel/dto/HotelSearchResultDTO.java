package com.goBhutan.adminPanel.hotel.dto;

import com.goBhutan.adminPanel.hotel.entity.Amenity;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class HotelSearchResultDTO {

    private Long hotelId;
    private String name;
    private String description;

    private String address;
    private String city;
    private String state;
    private String country;
    private String postalCode;

    private String phoneNumber;
    private String email;
    private String website;

    private Integer starRating;
    private BigDecimal startingFromPrice;

    private String primaryImageUrl;

    private List<AmenityDTO> amenities;

    @Data
    @Builder
    public static class AmenityDTO {
        private Long id;
        private String name;
        private String iconClass;
        private Amenity.AmenityCategory category;
    }
}