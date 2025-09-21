package com.goBhutan.adminPanel.theater.dto;

public class ScreeningCreateDTO {
    private String movieId;
    private String hallId;
    private String screeningDate;
    private String startTime;
    private java.math.BigDecimal vipPrice;
    private java.math.BigDecimal standardPrice;
    private java.math.BigDecimal economyPrice;

    // Constructors
    public ScreeningCreateDTO() {}

    // Getters and Setters
    public String getMovieId() { return movieId; }
    public void setMovieId(String movieId) { this.movieId = movieId; }

    public String getHallId() { return hallId; }
    public void setHallId(String hallId) { this.hallId = hallId; }

    public String getScreeningDate() { return screeningDate; }
    public void setScreeningDate(String screeningDate) { this.screeningDate = screeningDate; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public java.math.BigDecimal getVipPrice() { return vipPrice; }
    public void setVipPrice(java.math.BigDecimal vipPrice) { this.vipPrice = vipPrice; }

    public java.math.BigDecimal getStandardPrice() { return standardPrice; }
    public void setStandardPrice(java.math.BigDecimal standardPrice) { this.standardPrice = standardPrice; }

    public java.math.BigDecimal getEconomyPrice() { return economyPrice; }
    public void setEconomyPrice(java.math.BigDecimal economyPrice) { this.economyPrice = economyPrice; }
}
