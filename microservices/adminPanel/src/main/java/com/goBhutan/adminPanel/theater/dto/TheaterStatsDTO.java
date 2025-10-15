package com.goBhutan.adminPanel.theater.dto;

public class TheaterStatsDTO {
    private String theaterId;
    private String theaterName;
    private Long totalHalls;
    private Long totalSeats;
    private Long activeScreenings;

    // Constructors
    public TheaterStatsDTO() {}

    // Getters and Setters
    public String getTheaterId() { return theaterId; }
    public void setTheaterId(String theaterId) { this.theaterId = theaterId; }

    public String getTheaterName() { return theaterName; }
    public void setTheaterName(String theaterName) { this.theaterName = theaterName; }

    public Long getTotalHalls() { return totalHalls; }
    public void setTotalHalls(Long totalHalls) { this.totalHalls = totalHalls; }

    public Long getTotalSeats() { return totalSeats; }
    public void setTotalSeats(Long totalSeats) { this.totalSeats = totalSeats; }

    public Long getActiveScreenings() { return activeScreenings; }
    public void setActiveScreenings(Long activeScreenings) { this.activeScreenings = activeScreenings; }
}