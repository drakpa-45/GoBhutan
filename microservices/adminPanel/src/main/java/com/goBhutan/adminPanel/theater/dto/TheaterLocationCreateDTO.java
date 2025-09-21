package com.goBhutan.adminPanel.theater.dto;

public class TheaterLocationCreateDTO {
    private String dzongkhag;
    private String thromdoe;
    private String town;
    private String address;

    // Constructors
    public TheaterLocationCreateDTO() {}

    // Getters and Setters
    public String getDzongkhag() { return dzongkhag; }
    public void setDzongkhag(String dzongkhag) { this.dzongkhag = dzongkhag; }

    public String getThromdoe() { return thromdoe; }
    public void setThromdoe(String thromdoe) { this.thromdoe = thromdoe; }

    public String getTown() { return town; }
    public void setTown(String town) { this.town = town; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
