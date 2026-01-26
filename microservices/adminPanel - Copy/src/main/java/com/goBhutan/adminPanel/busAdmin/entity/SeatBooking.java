package com.goBhutan.adminPanel.busAdmin.entity;

import com.goBhutan.adminPanel.busAdmin.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_bs_seat_bookings",uniqueConstraints = @UniqueConstraint(columnNames = {"schedule_id", "seat_number"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    @Column(name = "applicant_cid", nullable = false)
    private String applicantCid;

    @Column(name = "applicant_mobile")
    private String applicantMobile;

    @Column(name = "applicant_email")
    private String applicantEmail;

    @Column(name = "seat_number", nullable = false)
    private Integer seatNumber;

    @Column(name = "seat_label", nullable = false)
    private String seatLabel;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status; // LOCKED, BOOKED, CANCELLED, EXPIRED

    private LocalDateTime lockExpiry;

    @Column(name = "payment_ref")
    private String paymentRef;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
