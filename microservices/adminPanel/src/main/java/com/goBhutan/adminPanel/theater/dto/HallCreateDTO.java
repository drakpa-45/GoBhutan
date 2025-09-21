package com.goBhutan.adminPanel.theater.dto;

public class HallCreateDTO {
    private String name;
    private String theaterId;
    private Integer totalSeats;

    // Constructors
    public HallCreateDTO() {}

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTheaterId() { return theaterId; }
    public void setTheaterId(String theaterId) { this.theaterId = theaterId; }

    public Integer getTotalSeats() { return totalSeats; }
    public void setTotalSeats(Integer totalSeats) { this.totalSeats = totalSeats; }
}

