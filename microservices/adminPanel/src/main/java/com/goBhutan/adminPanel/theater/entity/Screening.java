//package com.goBhutan.adminPanel.theater.entity;
//
//import jakarta.persistence.*;
//import org.hibernate.annotations.GenericGenerator;
//import java.math.BigDecimal;
//import java.time.Instant;
//import java.time.LocalDate;
//import java.util.List;
//import java.util.UUID;
//@Entity
//@Table(name = "ttbl_mvth_screenings")
//public class Screening {
//    @Id
//    @GeneratedValue(generator = "uuid2")
//    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
//    @Column(name = "id", updatable = false, nullable = false, unique = true)
//    private String id;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "movie_id", nullable = false)
//    private Movie movie;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "hall_id", nullable = false)
//    private Hall hall;
//
//    @Column(name = "screening_date", nullable = false)
//    private LocalDate screeningDate;
//
//    @Column(name = "start_time", nullable = false)
//    private Instant startTime;
//
//    @Column(name = "end_time", nullable = false)
//    private Instant endTime;
//
//    @Column(name = "vip_price", precision = 10, scale = 2)
//    private BigDecimal vipPrice;
//
//    @Column(name = "standard_price", precision = 10, scale = 2)
//    private BigDecimal standardPrice;
//
//    @Column(name = "economy_price", precision = 10, scale = 2)
//    private BigDecimal economyPrice;
//
//    @Column(name = "available_seats", nullable = false)
//    private Integer availableSeats;
//
//    @Column(name = "booked_seats")
//    private Integer bookedSeats = 0;
//
//    @Column(name = "is_active")
//    private Boolean isActive = true;
//
//    @Column(name = "created_at", nullable = false, updatable = false)
//    private Instant createdAt = Instant.now();
//
//    @OneToMany(mappedBy = "screening", cascade = CascadeType.ALL)
//    private List<Booking> bookings;
//
//    // Constructors
//    public Screening() {}
//
//    // Getters and Setters
//    public String getId() { return id; }
//    public void setId(String id) { this.id = id; }
//
//    public Movie getMovie() { return movie; }
//    public void setMovie(Movie movie) { this.movie = movie; }
//
//    public Hall getHall() { return hall; }
//    public void setHall(Hall hall) { this.hall = hall; }
//
//    public LocalDate getScreeningDate() { return screeningDate; }
//    public void setScreeningDate(LocalDate screeningDate) { this.screeningDate = screeningDate; }
//
//    public Instant getStartTime() { return startTime; }
//    public void setStartTime(Instant startTime) { this.startTime = startTime; }
//
//    public Instant getEndTime() { return endTime; }
//    public void setEndTime(Instant endTime) { this.endTime = endTime; }
//
//    public BigDecimal getVipPrice() { return vipPrice; }
//    public void setVipPrice(BigDecimal vipPrice) { this.vipPrice = vipPrice; }
//
//    public BigDecimal getStandardPrice() { return standardPrice; }
//    public void setStandardPrice(BigDecimal standardPrice) { this.standardPrice = standardPrice; }
//
//    public BigDecimal getEconomyPrice() { return economyPrice; }
//    public void setEconomyPrice(BigDecimal economyPrice) { this.economyPrice = economyPrice; }
//
//    public Integer getAvailableSeats() { return availableSeats; }
//    public void setAvailableSeats(Integer availableSeats) { this.availableSeats = availableSeats; }
//
//    public Integer getBookedSeats() { return bookedSeats; }
//    public void setBookedSeats(Integer bookedSeats) { this.bookedSeats = bookedSeats; }
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