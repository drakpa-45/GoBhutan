package com.goBhutan.adminPanel.theater.dto;

import java.util.List;

public class TheaterDTO {
    private String id;
    private String name;
    private String description;
    private TheaterLocationDTO location;
    private String ownerId;
    private String ownerUsername;
    private Boolean isActive;
    private String createdAt;
    private List<HallDTO> halls;

    // Constructors
    public TheaterDTO() {}

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public TheaterLocationDTO getLocation() { return location; }
    public void setLocation(TheaterLocationDTO location) { this.location = location; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public String getOwnerUsername() { return ownerUsername; }
    public void setOwnerUsername(String ownerUsername) { this.ownerUsername = ownerUsername; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public List<HallDTO> getHalls() { return halls; }
    public void setHalls(List<HallDTO> halls) { this.halls = halls; }
}
