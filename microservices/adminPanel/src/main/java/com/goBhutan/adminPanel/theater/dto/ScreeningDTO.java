package com.goBhutan.adminPanel.theater.dto;

public class ScreeningDTO {
    private String id;
    private MovieResponseDTO movie;
    private HallDTO hall;
    private String screeningDate;
    private String startTime;
    private String endTime;
    private java.math.BigDecimal vipPrice;
    private java.math.BigDecimal standardPrice;
    private java.math.BigDecimal economyPrice;
    private Integer availableSeats;
    private Integer bookedSeats;
    private Boolean isActive;
    private String createdAt;

    // Constructors
    public ScreeningDTO() {}

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public MovieResponseDTO getMovie() { return movie; }
    public void setMovie(MovieResponseDTO movie) { this.movie = movie; }

    public HallDTO getHall() { return hall; }
    public void setHall(HallDTO hall) { this.hall = hall; }

    public String getScreeningDate() { return screeningDate; }
    public void setScreeningDate(String screeningDate) { this.screeningDate = screeningDate; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public java.math.BigDecimal getVipPrice() { return vipPrice; }
    public void setVipPrice(java.math.BigDecimal vipPrice) { this.vipPrice = vipPrice; }

    public java.math.BigDecimal getStandardPrice() { return standardPrice; }
    public void setStandardPrice(java.math.BigDecimal standardPrice) { this.standardPrice = standardPrice; }

    public java.math.BigDecimal getEconomyPrice() { return economyPrice; }
    public void setEconomyPrice(java.math.BigDecimal economyPrice) { this.economyPrice = economyPrice; }

    public Integer getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(Integer availableSeats) { this.availableSeats = availableSeats; }

    public Integer getBookedSeats() { return bookedSeats; }
    public void setBookedSeats(Integer bookedSeats) { this.bookedSeats = bookedSeats; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}