package com.goBhutan.adminPanel.theater.dto;

import com.goBhutan.adminPanel.theater.dto.seat.SeatDTO;

public class BookingDTO {
    private Long id;
    private String bookingReference;
    private String userId;
    private String username;
    private SeatDTO seat;
    private java.math.BigDecimal pricePaid;
    private String status;
    private String bookingDate;
    private String paymentMethod;
    private String paymentReference;
    private Boolean isCancelled;
    private String cancelledAt;

    // Constructors
    public BookingDTO() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBookingReference() { return bookingReference; }
    public void setBookingReference(String bookingReference) { this.bookingReference = bookingReference; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public SeatDTO getSeat() { return seat; }
    public void setSeat(SeatDTO seat) { this.seat = seat; }

    public java.math.BigDecimal getPricePaid() { return pricePaid; }
    public void setPricePaid(java.math.BigDecimal pricePaid) { this.pricePaid = pricePaid; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getBookingDate() { return bookingDate; }
    public void setBookingDate(String bookingDate) { this.bookingDate = bookingDate; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getPaymentReference() { return paymentReference; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }

    public Boolean getIsCancelled() { return isCancelled; }
    public void setIsCancelled(Boolean isCancelled) { this.isCancelled = isCancelled; }

    public String getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(String cancelledAt) { this.cancelledAt = cancelledAt; }
}
