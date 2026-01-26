package com.goBhutan.adminPanel.hotel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class HotelResponseDTO {
    private Long id;
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
    private String adminUserId;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<ImageDTO> images;
    private List<AmenityDTO> amenities; // Changed from Set to List
    private Integer roomCount;
}