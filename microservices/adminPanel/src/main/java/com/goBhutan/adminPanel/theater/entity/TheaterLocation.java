//package com.goBhutan.adminPanel.theater.entity;
//
//import jakarta.persistence.*;
//import org.hibernate.annotations.GenericGenerator;
//import java.time.Instant;
//import java.util.List;
//import java.util.UUID;
//
//@Entity
//@Table(name = "ttbl_mvth_theater_locations")
//public class TheaterLocation {
//    @Id
//    @GeneratedValue(generator = "uuid2")
//    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
//    @Column(name = "id", updatable = false, nullable = false, unique = true)
//    private String id;
//
//    @Column(name = "dzongkhag", nullable = false)
//    private String dzongkhag;
//
//    @Column(name = "thromdoe")
//    private String thromdoe;
//
//    @Column(name = "town")
//    private String town;
//
//    @Column(name = "address")
//    private String address;
//
//    @Column(name = "created_at", nullable = false, updatable = false)
//    private Instant createdAt = Instant.now();
//
//    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL)
//    private List<Theater> theaters;
//
//    // Constructors
//    public TheaterLocation() {}
//
//    // Getters and Setters
//    public String getId() { return id; }
//    public void setId(String id) { this.id = id; }
//
//    public String getDzongkhag() { return dzongkhag; }
//    public void setDzongkhag(String dzongkhag) { this.dzongkhag = dzongkhag; }
//
//    public String getThromdoe() { return thromdoe; }
//    public void setThromdoe(String thromdoe) { this.thromdoe = thromdoe; }
//
//    public String getTown() { return town; }
//    public void setTown(String town) { this.town = town; }
//
//    public String getAddress() { return address; }
//    public void setAddress(String address) { this.address = address; }
//
//    public Instant getCreatedAt() { return createdAt; }
//    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
//
//    public List<Theater> getTheaters() { return theaters; }
//    public void setTheaters(List<Theater> theaters) { this.theaters = theaters; }
//
//    @PrePersist
//    public void ensureId() {
//        if (this.id == null) {
//            this.id = UUID.randomUUID().toString();
//        }
//    }
//}