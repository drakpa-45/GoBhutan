package com.goBhutan.adminPanel.theater.dto;

public class TheaterCreateDTO {
    private String name;
    private String description;
    private String locationId;

    // Constructors
    public TheaterCreateDTO() {}

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocationId() { return locationId; }
    public void setLocationId(String locationId) { this.locationId = locationId; }
}