package com.goBhutan.adminPanel.hotel.dto;

import com.goBhutan.adminPanel.hotel.entity.Room.RoomStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class RoomDTO {

    @NotBlank(message = "Room number is required")
    @Schema(description = "Room number", example = "101")
    private String roomNumber;

    @NotBlank(message = "Room size is required")
    @Schema(description = "Room size", example = "350 sq ft")
    private String roomSize;

    @NotNull(message = "Hotel ID is required")
    @Schema(description = "Hotel ID")
    private Long hotelId;

    @NotNull(message = "Floor number is required")
    @Min(value = 0, message = "Floor number must be at least 0")
    @Schema(description = "Floor number", example = "1")
    private Integer floor;

    @NotNull(message = "Base price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Base price must be greater than 0")
    @Schema(description = "Base price per night", example = "150.00")
    private BigDecimal basePrice;

    @Min(value = 1, message = "Max occupancy must be at least 1")
    @Schema(description = "Maximum occupancy", example = "2")
    private Integer maxOccupancy;

    @Schema(description = "Room status", example = "AVAILABLE")
    private RoomStatus status;

    @Schema(description = "Current check-in date")
    private LocalDate currentCheckInDate;

    @Schema(description = "Current check-out date")
    private LocalDate currentCheckOutDate;

    @Schema(description = "Room description")
    private String description;

    @Schema(description = "Is room active", example = "true")
    private Boolean isActive;

    @Schema(description = "List of amenity IDs")
    private List<Long> amenityIds;

    private Integer roomTypeId;

    // Getters and Setters
    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getRoomSize() {
        return roomSize;
    }

    public void setRoomSize(String roomSize) {
        this.roomSize = roomSize;
    }

    public Long getHotelId() {
        return hotelId;
    }

    public void setHotelId(Long hotelId) {
        this.hotelId = hotelId;
    }

    public Integer getFloor() {
        return floor;
    }

    public void setFloor(Integer floor) {
        this.floor = floor;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public Integer getMaxOccupancy() {
        return maxOccupancy;
    }

    public void setMaxOccupancy(Integer maxOccupancy) {
        this.maxOccupancy = maxOccupancy;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public void setStatus(RoomStatus status) {
        this.status = status;
    }

    public LocalDate getCurrentCheckInDate() {
        return currentCheckInDate;
    }

    public void setCurrentCheckInDate(LocalDate currentCheckInDate) {
        this.currentCheckInDate = currentCheckInDate;
    }

    public LocalDate getCurrentCheckOutDate() {
        return currentCheckOutDate;
    }

    public void setCurrentCheckOutDate(LocalDate currentCheckOutDate) {
        this.currentCheckOutDate = currentCheckOutDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public List<Long> getAmenityIds() {
        return amenityIds;
    }

    public void setAmenityIds(List<Long> amenityIds) {
        this.amenityIds = amenityIds;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public Integer getRoomTypeId() {
        return roomTypeId;
    }

    public void setRoomTypeId(Integer roomTypeId) {
        this.roomTypeId = roomTypeId;
    }
}