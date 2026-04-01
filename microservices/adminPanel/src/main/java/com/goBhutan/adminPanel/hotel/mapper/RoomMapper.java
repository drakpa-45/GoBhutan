package com.goBhutan.adminPanel.hotel.mapper;

import com.goBhutan.adminPanel.hotel.dto.AmenityDTO;
import com.goBhutan.adminPanel.hotel.dto.ImageDTO;
import com.goBhutan.adminPanel.hotel.dto.RoomResponseDTO;
import com.goBhutan.adminPanel.hotel.entity.Amenity;
import com.goBhutan.adminPanel.hotel.entity.HotelImage;
import com.goBhutan.adminPanel.hotel.entity.Room;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class RoomMapper {

    public RoomResponseDTO toRoomResponseDTO(Room room) {
        RoomResponseDTO dto = new RoomResponseDTO();
        dto.setId(room.getId());
        dto.setRoomNumber(room.getRoomNumber());
        dto.setRoomSize(room.getRoomSize());
        dto.setHotelId(room.getHotel().getId());
        dto.setHotelName(room.getHotel().getName());
        dto.setFloor(room.getFloor());
        dto.setBasePrice(room.getBasePrice());
        dto.setMaxOccupancy(room.getMaxOccupancy());
        dto.setStatus(room.getStatus());
        dto.setCurrentCheckInDate(room.getCurrentCheckInDate());
        dto.setCurrentCheckOutDate(room.getCurrentCheckOutDate());
        dto.setDescription(room.getDescription());
        dto.setIsActive(room.getIsActive());
        dto.setCreatedAt(room.getCreatedAt());
        dto.setUpdatedAt(room.getUpdatedAt());
        dto.setVersion(room.getVersion());

        // Map amenities
        if (room.getAmenities() != null) {
            dto.setAmenities(room.getAmenities().stream()
                    .map(this::toAmenityDTO)
                    .collect(Collectors.toList()));
        }

        // Map images
        if (room.getImages() != null && !room.getImages().isEmpty()) {
            dto.setImages(room.getImages().stream()
                    .map(RoomMapper::toImageDTO)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    private AmenityDTO toAmenityDTO(Amenity amenity) {
        AmenityDTO dto = new AmenityDTO();
        dto.setId(amenity.getId());
        dto.setName(amenity.getName());
        dto.setDescription(amenity.getDescription());
        dto.setIconClass(amenity.getIconClass());
        dto.setCategory(amenity.getCategory());
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
}