package com.goBhutan.adminPanel.theater.dto;

import java.math.BigDecimal;

public class BookingStatsDTO {
    private Long totalBookings;
    private Long confirmedBookings;
    private Long cancelledBookings;
    private BigDecimal totalRevenue;
    private Double occupancyRate;

    // Constructors
    public BookingStatsDTO() {}

    public BookingStatsDTO(Long totalBookings, Long confirmedBookings, Long cancelledBookings,
                           BigDecimal totalRevenue, Double occupancyRate) {
        this.totalBookings = totalBookings;
        this.confirmedBookings = confirmedBookings;
        this.cancelledBookings = cancelledBookings;
        this.totalRevenue = totalRevenue;
        this.occupancyRate = occupancyRate;
    }

    // Getters and Setters
    public Long getTotalBookings() { return totalBookings; }
    public void setTotalBookings(Long totalBookings) { this.totalBookings = totalBookings; }

    public Long getConfirmedBookings() { return confirmedBookings; }
    public void setConfirmedBookings(Long confirmedBookings) { this.confirmedBookings = confirmedBookings; }

    public Long getCancelledBookings() { return cancelledBookings; }
    public void setCancelledBookings(Long cancelledBookings) { this.cancelledBookings = cancelledBookings; }

    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }

    public Double getOccupancyRate() { return occupancyRate; }
    public void setOccupancyRate(Double occupancyRate) { this.occupancyRate = occupancyRate; }
}

