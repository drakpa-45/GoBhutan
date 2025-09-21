package com.goBhutan.adminPanel.theater.dto;

public class TheaterLocationDTO {
    private String id;
    private String dzongkhag;
    private String thromdoe;
    private String town;
    private String address;
    private String createdAt;

    // Constructors
    public TheaterLocationDTO() {}

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDzongkhag() { return dzongkhag; }
    public void setDzongkhag(String dzongkhag) { this.dzongkhag = dzongkhag; }

    public String getThromdoe() { return thromdoe; }
    public void setThromdoe(String thromdoe) { this.thromdoe = thromdoe; }

    public String getTown() { return town; }
    public void setTown(String town) { this.town = town; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
