package com.goBhutan.adminPanel.theater.dto;

import java.math.BigDecimal;
import java.util.List;

public class BookingResponseDTO {
    private List<BookingDTO> bookings;
    private BigDecimal totalAmount;
    private String message;

    // Constructors
    public BookingResponseDTO() {}

    public BookingResponseDTO(List<BookingDTO> bookings, BigDecimal totalAmount, String message) {
        this.bookings = bookings;
        this.totalAmount = totalAmount;
        this.message = message;
    }

    // Getters and Setters
    public List<BookingDTO> getBookings() { return bookings; }
    public void setBookings(List<BookingDTO> bookings) { this.bookings = bookings; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
