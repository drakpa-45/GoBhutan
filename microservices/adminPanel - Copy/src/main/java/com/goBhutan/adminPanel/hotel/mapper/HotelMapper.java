package com.goBhutan.adminPanel.hotel.mapper;

import com.goBhutan.adminPanel.hotel.dto.*;
import com.goBhutan.adminPanel.hotel.entity.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class HotelMapper {

    // ============= Entity to DTO Conversions =============

    /**
     * Convert Amenity Entity to AmenityDTO
     */
    public static AmenityDTO toAmenityDTO(Amenity amenity) {
        if (amenity == null) return null;

        AmenityDTO dto = new AmenityDTO();
        dto.setId(amenity.getId());
        dto.setName(amenity.getName());
        dto.setDescription(amenity.getDescription());
        dto.setIconClass(amenity.getIconClass());
        dto.setCategory(amenity.getCategory() != null ? amenity.getCategory() : null);

        return dto;
    }

    /**
     * Convert HotelImage Entity to ImageDTO
     */
    public static ImageDTO toImageDTO(HotelImage image) {
        if (image == null) return null;

        ImageDTO dto = new ImageDTO();
        dto.setId(image.getId());
        dto.setUrl(image.getUrl());
        dto.setTitle(image.getTitle());
        dto.setCaption(image.getCaption());
        dto.setDisplayOrder(image.getDisplayOrder());
        dto.setIsPrimary(image.getIsPrimary());
        dto.setCreatedAt(image.getCreatedAt());

        if (image.getHotel() != null) {
            dto.setHotelId(image.getHotel().getId());
        }

        if (image.getRoom() != null) {
            dto.setRoomId(image.getRoom().getId());
        }

        return dto;
    }


    /**
     * Convert Hotel Entity to HotelResponseDTO
     */
    public static HotelResponseDTO toHotelResponseDTO(Hotel hotel) {
        if (hotel == null) return null;

        HotelResponseDTO dto = new HotelResponseDTO();
        dto.setId(hotel.getId());
        dto.setName(hotel.getName());
        dto.setDescription(hotel.getDescription());
        dto.setAddress(hotel.getAddress());
        dto.setCity(hotel.getCity());
        dto.setState(hotel.getState());
        dto.setCountry(hotel.getCountry());
        dto.setPostalCode(hotel.getPostalCode());
        dto.setPhoneNumber(hotel.getPhoneNumber());
        dto.setEmail(hotel.getEmail());
        dto.setWebsite(hotel.getWebsite());
        dto.setStarRating(hotel.getStarRating());
        dto.setAdminUserId(hotel.getAdminUserId());
        dto.setIsActive(hotel.getIsActive());
        dto.setCreatedAt(hotel.getCreatedAt());
        dto.setUpdatedAt(hotel.getUpdatedAt());

        // Map images
        if (hotel.getImages() != null && !hotel.getImages().isEmpty()) {
            dto.setImages(hotel.getImages().stream()
                    .map(HotelMapper::toImageDTO)
                    .collect(Collectors.toList()));
        }

        // Map amenities (OneToMany relationship)
        if (hotel.getAmenities() != null && !hotel.getAmenities().isEmpty()) {
            dto.setAmenities(hotel.getAmenities().stream()
                    .map(HotelMapper::toAmenityDTO)
                    .collect(Collectors.toList()));
        }

        // Map rooms count
        if (hotel.getRooms() != null && !hotel.getRooms().isEmpty()) {
            dto.setRoomCount(hotel.getRooms().size());
        } else {
            dto.setRoomCount(0);
        }

        return dto;
    }

    // ============= DTO to Entity Conversions =============

    /**
     * Convert HotelDTO to Hotel Entity
     * Note: Does NOT map amenities or images - they are handled separately in the service
     */
    public static Hotel toEntity(HotelDTO dto) {
        if (dto == null) return null;

        Hotel hotel = new Hotel();
        hotel.setName(dto.getName());
        hotel.setDescription(dto.getDescription());
        hotel.setAddress(dto.getAddress());
        hotel.setCity(dto.getCity());
        hotel.setState(dto.getState());
        hotel.setCountry(dto.getCountry());
        hotel.setPostalCode(dto.getPostalCode());
        hotel.setPhoneNumber(dto.getPhoneNumber());
        hotel.setEmail(dto.getEmail());
        hotel.setWebsite(dto.getWebsite());
        hotel.setStarRating(dto.getStarRating());
        hotel.setAdminUserId(dto.getAdminUserId());

        // Don't map amenities or images here - handled in service layer
        return hotel;
    }

    /**
     * Convert AmenityDTO to Amenity Entity
     * Note: Does NOT set the hotel reference - that's done in the service
     */
    public static Amenity toAmenityEntity(AmenityDTO dto) {
        if (dto == null) return null;

        Amenity amenity = new Amenity();
        amenity.setId(dto.getId());
        amenity.setName(dto.getName());
        amenity.setDescription(dto.getDescription());
        amenity.setIconClass(dto.getIconClass());

        // Convert category from String to Enum
        if (dto.getCategory() != null) {
            amenity.setCategory(Amenity.AmenityCategory.valueOf(String.valueOf(dto.getCategory())));
        }

        return amenity;
    }

    /**
     * Update amenities for a hotel
     * Handles the OneToMany relationship properly
     */
    public static void updateAmenities(Hotel hotel, List<AmenityDTO> amenityDTOs) {
        if (hotel == null) return;

        // Clear existing amenities
        if (hotel.getAmenities() != null) {
            hotel.getAmenities().clear();
        }

        // Add new amenities
        if (amenityDTOs != null && !amenityDTOs.isEmpty()) {
            for (AmenityDTO amenityDTO : amenityDTOs) {
                Amenity amenity = toAmenityEntity(amenityDTO);
                hotel.addAmenity(amenity); // Use helper method to set both sides
            }
        }
    }

    /**
     * Convert list of AmenityDTOs to list of Amenity entities
     */
    public static List<Amenity> toAmenityEntityList(List<AmenityDTO> amenityDTOs) {
        if (amenityDTOs == null || amenityDTOs.isEmpty()) {
            return Collections.emptyList();
        }

        return amenityDTOs.stream()
                .map(HotelMapper::toAmenityEntity)
                .collect(Collectors.toList());
    }

    /**
     * Convert list of Amenities to list of AmenityDTOs
     */
    public static List<AmenityDTO> toAmenityDTOList(List<Amenity> amenities) {
        if (amenities == null || amenities.isEmpty()) {
            return Collections.emptyList();
        }

        return amenities.stream()
                .map(HotelMapper::toAmenityDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convert list of HotelImages to list of ImageDTOs
     */
    public static List<ImageDTO> toImageDTOList(List<HotelImage> images) {
        if (images == null || images.isEmpty()) {
            return Collections.emptyList();
        }

        return images.stream()
                .map(HotelMapper::toImageDTO)
                .collect(Collectors.toList());
    }
}