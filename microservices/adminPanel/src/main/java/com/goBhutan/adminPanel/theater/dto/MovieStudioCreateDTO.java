package com.goBhutan.adminPanel.theater.dto;

public class MovieStudioCreateDTO {
    private String name;
    private String description;
    private String country;
    private String website;

    // Constructors
    public MovieStudioCreateDTO() {}

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
}
