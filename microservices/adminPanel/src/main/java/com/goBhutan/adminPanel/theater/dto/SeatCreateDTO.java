package com.goBhutan.adminPanel.theater.dto;

public class SeatCreateDTO {
    private String seatNumber;
    private String rowName;
    private String seatClass;
    private String hallId;

    // Constructors
    public SeatCreateDTO() {}

    // Getters and Setters
    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }

    public String getRowName() { return rowName; }
    public void setRowName(String rowName) { this.rowName = rowName; }

    public String getSeatClass() { return seatClass; }
    public void setSeatClass(String seatClass) { this.seatClass = seatClass; }

    public String getHallId() { return hallId; }
    public void setHallId(String hallId) { this.hallId = hallId; }
}
