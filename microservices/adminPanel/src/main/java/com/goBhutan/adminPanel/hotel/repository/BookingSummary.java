package com.goBhutan.adminPanel.hotel.repository;

public interface BookingSummary {
    Long getId();
    String getCid();
    String getGuestName();
    String getRoomNumber();
    String getStatus();
    String getBookingReference();
}
