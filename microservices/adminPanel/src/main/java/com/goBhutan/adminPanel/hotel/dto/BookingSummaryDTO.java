package com.goBhutan.adminPanel.hotel.dto;

public class BookingSummaryDTO {
    private Long bookingId;
    private String guestName;
    private String roomNumber;
    private String status;
    private String bookingReference;

    public BookingSummaryDTO(Long bookingId, String guestName, String roomNumber,
                             String status, String bookingReference) {
        this.bookingId = bookingId;
        this.guestName = guestName;
        this.roomNumber = roomNumber;
        this.status = status;
        this.bookingReference = bookingReference;
    }

    // getters + setters

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBookingReference() {
        return bookingReference;
    }

    public void setBookingReference(String bookingReference) {
        this.bookingReference = bookingReference;
    }
}
