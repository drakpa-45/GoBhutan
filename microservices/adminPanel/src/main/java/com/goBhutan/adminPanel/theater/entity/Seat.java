//package com.goBhutan.adminPanel.theater.entity;
//
//import jakarta.persistence.*;
//import org.hibernate.annotations.GenericGenerator;
//import java.time.Instant;
//import java.util.List;
//import java.util.UUID;
//@Entity
//@Table(name = "ttbl_mvth_seats")
//public class Seat {
//    @Id
//    @GeneratedValue(generator = "uuid2")
//    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
//    @Column(name = "id", updatable = false, nullable = false, unique = true)
//    private String id;
//
//    @Column(name = "seat_number", nullable = false)
//    private String seatNumber;
//
//    @Column(name = "row_name", nullable = false)
//    private String rowName;
//
//    @Enumerated(EnumType.STRING)
//    @Column(name = "seat_class", nullable = false)
//    private SeatClass seatClass;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "hall_id", nullable = false)
//    private Hall hall;
//
//    @Column(name = "is_blocked")
//    private Boolean isBlocked = false;
//
//    @Column(name = "is_active")
//    private Boolean isActive = true;
//
//    @Column(name = "created_at", nullable = false, updatable = false)
//    private Instant createdAt = Instant.now();
//
//    @OneToMany(mappedBy = "seat", cascade = CascadeType.ALL)
//    private List<Booking> bookings;
//
//    public enum SeatClass {
//        VIP, STANDARD, ECONOMY
//    }
//
//    // Constructors
//    public Seat() {}
//
//    // Getters and Setters
//    public String getId() { return id; }
//    public void setId(String id) { this.id = id; }
//
//    public String getSeatNumber() { return seatNumber; }
//    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }
//
//    public String getRowName() { return rowName; }
//    public void setRowName(String rowName) { this.rowName = rowName; }
//
//    public SeatClass getSeatClass() { return seatClass; }
//    public void setSeatClass(SeatClass seatClass) { this.seatClass = seatClass; }
//
//    public Hall getHall() { return hall; }
//    public void setHall(Hall hall) { this.hall = hall; }
//
//    public Boolean getIsBlocked() { return isBlocked; }
//    public void setIsBlocked(Boolean isBlocked) { this.isBlocked = isBlocked; }
//
//    public Boolean getIsActive() { return isActive; }
//    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
//
//    public Instant getCreatedAt() { return createdAt; }
//    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
//
//    public List<Booking> getBookings() { return bookings; }
//    public void setBookings(List<Booking> bookings) { this.bookings = bookings; }
//
//    @PrePersist
//    public void ensureId() {
//        if (this.id == null) {
//            this.id = UUID.randomUUID().toString();
//        }
//    }
//}