package com.goBhutan.adminPanel.hotel.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "tbl_ht_room_types")
public class RoomType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Room type name is required")
    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "bed_count")
    private Integer bedCount;

    @Column(name = "bed_type")
    private String bedType;

    @Column(name = "room_size")
    private String roomSize;

    // ✅ Add relation to RoomTypeMaster
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_type_master_id")
    private RoomTypeMaster roomTypeMaster;

    public RoomType() {}

    // Getters & Setters
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

    public RoomTypeMaster getRoomTypeMaster() { return roomTypeMaster; }
    public void setRoomTypeMaster(RoomTypeMaster roomTypeMaster) { this.roomTypeMaster = roomTypeMaster; }
}
