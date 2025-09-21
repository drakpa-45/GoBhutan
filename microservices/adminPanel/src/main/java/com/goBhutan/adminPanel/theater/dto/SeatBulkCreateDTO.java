package com.goBhutan.adminPanel.theater.dto;

public class SeatBulkCreateDTO {
    private String hallId;
    private Integer rows;
    private Integer seatsPerRow;
    private String seatClass;
    private String startingRow;

    // Constructors
    public SeatBulkCreateDTO() {}

    // Getters and Setters
    public String getHallId() { return hallId; }
    public void setHallId(String hallId) { this.hallId = hallId; }

    public Integer getRows() { return rows; }
    public void setRows(Integer rows) { this.rows = rows; }

    public Integer getSeatsPerRow() { return seatsPerRow; }
    public void setSeatsPerRow(Integer seatsPerRow) { this.seatsPerRow = seatsPerRow; }

    public String getSeatClass() { return seatClass; }
    public void setSeatClass(String seatClass) { this.seatClass = seatClass; }

    public String getStartingRow() { return startingRow; }
    public void setStartingRow(String startingRow) { this.startingRow = startingRow; }
}

