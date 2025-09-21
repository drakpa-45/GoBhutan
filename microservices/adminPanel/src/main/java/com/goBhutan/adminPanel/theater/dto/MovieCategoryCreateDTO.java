package com.goBhutan.adminPanel.theater.dto;

public class MovieCategoryCreateDTO {
    private String name;
    private String description;

    // Constructors
    public MovieCategoryCreateDTO() {}

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
