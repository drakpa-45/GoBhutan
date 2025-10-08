package com.goBhutan.adminPanel.hotel.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.goBhutan.adminPanel.hotel.entity.Room;
import com.goBhutan.adminPanel.hotel.entity.RoomType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomResponseDTO {
    private Long id;
    private String roomNumber;
    private Long hotelId;
    private String hotelName;
    private Long roomTypeId;
    private String roomTypeName;
    private Integer floor;
    private BigDecimal basePrice;
    private Integer maxOccupancy;
    private String status;        // Available, Occupied, etc.
    private Boolean isActive;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ✅ Custom constructor from Room entity
    public RoomResponseDTO(Room room) {
        this.id = room.getId();
        this.roomNumber = room.getRoomNumber();

        if (room.getHotel() != null) {
            this.hotelId = room.getHotel().getId();
            this.hotelName = room.getHotel().getName();
        }

        if (room.getRoomType() != null) {
            this.roomTypeId = room.getRoomType().getId();
            this.roomTypeName = room.getRoomType().getName();
        }

        this.floor = room.getFloor();
        this.basePrice = room.getBasePrice();
        this.maxOccupancy = room.getMaxOccupancy();
        this.status = room.getStatus() != null ? room.getStatus().name() : null;
        this.isActive = room.getIsActive();
        this.description = room.getDescription();
        this.createdAt = room.getCreatedAt();
        this.updatedAt = room.getUpdatedAt();
    }
}
