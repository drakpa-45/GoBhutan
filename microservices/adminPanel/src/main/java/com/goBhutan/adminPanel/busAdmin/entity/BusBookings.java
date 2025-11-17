package com.goBhutan.adminPanel.busAdmin.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.goBhutan.adminPanel.busAdmin.enums.BookingStatus;
import com.goBhutan.adminPanel.hotel.entity.Booking;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tbl_bs_t_bookings")
public class BusBookings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Booking reference is required")
    @Column(name = "booking_reference", nullable = false, unique = true)
    private String bookingReference;

    @Column(name = "user_id")
    private String userId;

    @NotBlank(message = "Passenger name is required")
    @Column(name = "passenger_name", nullable = false)
    private String passengerName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Column(name = "email", nullable = false)
    private String email;

    @NotBlank(message = "Phone number is required")
    @Column(name = "phone", nullable = false)
    private String phone;

    @NotNull(message = "Total fare is required")
    @Column(name = "total_fare", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalFare;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BookingStatus status = BookingStatus.PENDING;

    @Column(name = "booking_date", nullable = false)
    private LocalDateTime bookingDate = LocalDateTime.now();

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason")
    private String cancellationReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    @JsonIgnore
    private Schedule schedule;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<BookingSeat> bookingSeats = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (bookingReference == null) {
            bookingReference = generateBookingReference();
        }
    }

    private String generateBookingReference() {
        return "BKG" + System.currentTimeMillis() + (int)(Math.random() * 1000);
    }
}
