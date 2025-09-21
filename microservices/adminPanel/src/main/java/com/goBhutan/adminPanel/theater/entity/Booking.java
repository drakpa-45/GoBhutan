//package com.goBhutan.adminPanel.theater.entity;
//
//import jakarta.persistence.*;
//import org.hibernate.annotations.GenericGenerator;
//import java.math.BigDecimal;
//import java.time.Instant;
//import java.util.UUID;
//@Entity
//@Table(name = "ttbl_mvth_bookings")
//public class Booking {
//    @Id
//    @GeneratedValue(generator = "uuid2")
//    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
//    @Column(name = "id", updatable = false, nullable = false, unique = true)
//    private String id;
//
//    @Column(name = "booking_reference", unique = true, nullable = false)
//    private String bookingReference;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id", nullable = false)
//    private com.goBhutan.adminPanel.common.entity.AppUser user;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "screening_id", nullable = false)
//    private Screening screening;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "seat_id", nullable = false)
//    private Seat seat;
//
//    @Column(name = "price_paid", precision = 10, scale = 2, nullable = false)
//    private BigDecimal pricePaid;
//
//    @Enumerated(EnumType.STRING)
//    @Column(name = "status", nullable = false)
//    private BookingStatus status = BookingStatus.CONFIRMED;
//
//    @Column(name = "booking_date", nullable = false)
//    private Instant bookingDate = Instant.now();
//
//    @Column(name = "payment_method")
//    private String paymentMethod;
//
//    @Column(name = "payment_reference")
//    private String paymentReference;
//
//    @Column(name = "is_cancelled")
//    private Boolean isCancelled = false;
//
//    @Column(name = "cancelled_at")
//    private Instant cancelledAt;
//
//    public enum BookingStatus {
//        CONFIRMED, CANCELLED, REFUNDED, USED
//    }
//
//    // Constructors
//    public Booking() {}
//
//    // Getters and Setters
//    public String getId() { return id; }
//    public void setId(String id) { this.id = id; }
//
//    public String getBookingReference() { return bookingReference; }
//    public void setBookingReference(String bookingReference) { this.bookingReference = bookingReference; }
//
//    public com.goBhutan.adminPanel.common.entity.AppUser getUser() { return user; }
//    public void setUser(com.goBhutan.adminPanel.common.entity.AppUser user) { this.user = user; }
//
//    public Screening getScreening() { return screening; }
//    public void setScreening(Screening screening) { this.screening = screening; }
//
//    public Seat getSeat() { return seat; }
//    public void setSeat(Seat seat) { this.seat = seat; }
//
//    public BigDecimal getPricePaid() { return pricePaid; }
//    public void setPricePaid(BigDecimal pricePaid) { this.pricePaid = pricePaid; }
//
//    public BookingStatus getStatus() { return status; }
//    public void setStatus(BookingStatus status) { this.status = status; }
//
//    public Instant getBookingDate() { return bookingDate; }
//    public void setBookingDate(Instant bookingDate) { this.bookingDate = bookingDate; }
//
//    public String getPaymentMethod() { return paymentMethod; }
//    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
//
//    public String getPaymentReference() { return paymentReference; }
//    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }
//
//    public Boolean getIsCancelled() { return isCancelled; }
//    public void setIsCancelled(Boolean isCancelled) { this.isCancelled = isCancelled; }
//
//    public Instant getCancelledAt() { return cancelledAt; }
//    public void setCancelledAt(Instant cancelledAt) { this.cancelledAt = cancelledAt; }
//
//    @PrePersist
//    public void ensureId() {
//        if (this.id == null) {
//            this.id = UUID.randomUUID().toString();
//        }
//        if (this.bookingReference == null) {
//            this.bookingReference = "BK" + System.currentTimeMillis();
//        }
//    }
//}