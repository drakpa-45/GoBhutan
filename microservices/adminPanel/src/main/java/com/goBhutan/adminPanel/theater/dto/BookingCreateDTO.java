package com.goBhutan.adminPanel.theater.dto;

import java.util.List;

public class BookingCreateDTO {
    private String screeningId;
    private List<String> seatIds;
    private String paymentMethod;
    private String paymentReference;

    // Constructors
    public BookingCreateDTO() {}

    // Getters and Setters
    public String getScreeningId() { return screeningId; }
    public void setScreeningId(String screeningId) { this.screeningId = screeningId; }

    public List<String> getSeatIds() { return seatIds; }
    public void setSeatIds(List<String> seatIds) { this.seatIds = seatIds; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getPaymentReference() { return paymentReference; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }
}
