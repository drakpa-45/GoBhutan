package com.goBhutan.adminPanel.busAdmin.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class RevenueReport {
    private BigDecimal totalRevenue;
    private Integer totalBookings;
    private List<BookingInfo> bookings;
    @Data
    public static class BookingInfo {
        private Long id;
        private String passengerName;
        private String passengerEmail;
        private Integer seatNumber;
        private BigDecimal totalAmount;
        private String busNumber;
        private String route;
        private String departureTime;
        private String status;

        // Constructors
        public BookingInfo() {}

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getPassengerName() { return passengerName; }
        public void setPassengerName(String passengerName) { this.passengerName = passengerName; }

        public String getPassengerEmail() { return passengerEmail; }
        public void setPassengerEmail(String passengerEmail) { this.passengerEmail = passengerEmail; }

        public Integer getSeatNumber() { return seatNumber; }
        public void setSeatNumber(Integer seatNumber) { this.seatNumber = seatNumber; }

        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

        public String getBusNumber() { return busNumber; }
        public void setBusNumber(String busNumber) { this.busNumber = busNumber; }

        public String getRoute() { return route; }
        public void setRoute(String route) { this.route = route; }

        public String getDepartureTime() { return departureTime; }
        public void setDepartureTime(String departureTime) { this.departureTime = departureTime; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
    // Constructors
    public RevenueReport() {}

    public RevenueReport(BigDecimal totalRevenue, Integer totalBookings, List<BookingInfo> bookings) {
        this.totalRevenue = totalRevenue;
        this.totalBookings = totalBookings;
        this.bookings = bookings;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }
    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
    public Integer getTotalBookings() {
        return totalBookings;
    }
    public void setTotalBookings(Integer totalBookings) {
        this.totalBookings = totalBookings;
    }
    public List<BookingInfo> getBookings() {
        return bookings;
    }
    public void setBookings(List<BookingInfo> bookings) {
        this.bookings = bookings;
    }
}
