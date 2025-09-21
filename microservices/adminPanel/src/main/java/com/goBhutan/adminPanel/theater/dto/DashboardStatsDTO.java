package com.goBhutan.adminPanel.theater.dto;

import java.math.BigDecimal;

public class DashboardStatsDTO {
    private Long totalMovies;
    private Long upcomingMovies;
    private Long currentlyRunningMovies;
    private Double seatOccupancyRate;
    private BigDecimal totalRevenue;
    private BigDecimal todayRevenue;
    private Long totalBookings;
    private Long todayBookings;

    // Constructors
    public DashboardStatsDTO() {}

    public DashboardStatsDTO(Long totalMovies, Long upcomingMovies, Long currentlyRunningMovies,
                             Double seatOccupancyRate, BigDecimal totalRevenue, BigDecimal todayRevenue,
                             Long totalBookings, Long todayBookings) {
        this.totalMovies = totalMovies;
        this.upcomingMovies = upcomingMovies;
        this.currentlyRunningMovies = currentlyRunningMovies;
        this.seatOccupancyRate = seatOccupancyRate;
        this.totalRevenue = totalRevenue;
        this.todayRevenue = todayRevenue;
        this.totalBookings = totalBookings;
        this.todayBookings = todayBookings;
    }

    // Getters and Setters
    public Long getTotalMovies() { return totalMovies; }
    public void setTotalMovies(Long totalMovies) { this.totalMovies = totalMovies; }

    public Long getUpcomingMovies() { return upcomingMovies; }
    public void setUpcomingMovies(Long upcomingMovies) { this.upcomingMovies = upcomingMovies; }

    public Long getCurrentlyRunningMovies() { return currentlyRunningMovies; }
    public void setCurrentlyRunningMovies(Long currentlyRunningMovies) { this.currentlyRunningMovies = currentlyRunningMovies; }

    public Double getSeatOccupancyRate() { return seatOccupancyRate; }
    public void setSeatOccupancyRate(Double seatOccupancyRate) { this.seatOccupancyRate = seatOccupancyRate; }

    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }

    public BigDecimal getTodayRevenue() { return todayRevenue; }
    public void setTodayRevenue(BigDecimal todayRevenue) { this.todayRevenue = todayRevenue; }

    public Long getTotalBookings() { return totalBookings; }
    public void setTotalBookings(Long totalBookings) { this.totalBookings = totalBookings; }

    public Long getTodayBookings() { return todayBookings; }
    public void setTodayBookings(Long todayBookings) { this.todayBookings = todayBookings; }
}