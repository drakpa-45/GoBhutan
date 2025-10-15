package com.goBhutan.adminPanel.hotel.mapper;

import com.goBhutan.adminPanel.hotel.dto.AmenityDTO;
import com.goBhutan.adminPanel.hotel.dto.HotelResponseDTO;
import com.goBhutan.adminPanel.hotel.dto.RoomDTO;
import com.goBhutan.adminPanel.hotel.dto.RoomTypeDTO;
import com.goBhutan.adminPanel.hotel.entity.Amenity;
import com.goBhutan.adminPanel.hotel.entity.Hotel;
import com.goBhutan.adminPanel.hotel.entity.Room;
import com.goBhutan.adminPanel.hotel.entity.RoomType;

import java.util.stream.Collectors;

public class HotelMapper {

    public static AmenityDTO toAmenityDTO(Amenity amenity) {
        return new AmenityDTO(
                amenity.getId(),
                amenity.getName(),
                amenity.getDescription(),
                amenity.getIconClass(),
                amenity.getCategory() != null ? amenity.getCategory().name() : null
        );
    }

    public static RoomTypeDTO toRoomTypeDTO(RoomType roomType) {
        if (roomType == null) return null;
        return new RoomTypeDTO(
                roomType.getId(),
                roomType.getName(),
                roomType.getDescription(),
                roomType.getBedCount(),
                roomType.getBedType(),
                roomType.getRoomSize()
        );
    }

    public static RoomDTO toRoomDTO(Room room) {
        return new RoomDTO(
                room.getId(),
                room.getRoomNumber(),
                toRoomTypeDTO(room.getRoomType()),
                room.getFloor(),
                room.getBasePrice(),
                room.getMaxOccupancy(),
                room.getStatus() != null ? room.getStatus().name() : null,
                room.getCurrentCheckInDate(),
                room.getCurrentCheckOutDate(),
                room.getIsActive(),
                room.getDescription(),
                room.getAmenities().stream()
                        .map(HotelMapper::toAmenityDTO)
                        .collect(Collectors.toSet())
        );
    }

    public static HotelResponseDTO toHotelResponseDTO(Hotel hotel) {
        return new HotelResponseDTO(
                hotel.getId(),
                hotel.getName(),
                hotel.getDescription(),
                hotel.getAddress(),
                hotel.getCity(),
                hotel.getState(),
                hotel.getCountry(),
                hotel.getPostalCode(),
                hotel.getPhoneNumber(),
                hotel.getEmail(),
                hotel.getWebsite(),
                hotel.getStarRating(),
                hotel.getIsActive(),
                hotel.getCreatedAt(),
                hotel.getUpdatedAt(),
                hotel.getAmenities().stream()
                        .map(HotelMapper::toAmenityDTO)
                        .collect(Collectors.toSet()),
                hotel.getRooms().stream()
                        .map(HotelMapper::toRoomDTO)
                        .collect(Collectors.toList())
        );
    }
}
