package com.goBhutan.adminPanel.hotel.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "tbl_ht_room_types")
public class RoomType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Room type name is required")
    @Column(nullable = false, unique = true)
    private String name; // Single, Double, Suite, Deluxe, etc.
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "bed_count")
    private Integer bedCount;
    
    @Column(name = "bed_type")
    private String bedType; // Single, Double, Queen, King
    
    @Column(name = "room_size")
    private String roomSize; // in sq ft or sq m
    
    public RoomType() {}
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Integer getBedCount() { return bedCount; }
    public void setBedCount(Integer bedCount) { this.bedCount = bedCount; }
    
    public String getBedType() { return bedType; }
    public void setBedType(String bedType) { this.bedType = bedType; }
    
    public String getRoomSize() { return roomSize; }
    public void setRoomSize(String roomSize) { this.roomSize = roomSize; }
}

