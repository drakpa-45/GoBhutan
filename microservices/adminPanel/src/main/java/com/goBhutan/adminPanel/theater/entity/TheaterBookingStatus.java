package com.goBhutan.adminPanel.theater.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tbl_mvth_booking_status")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TheaterBookingStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "status_name", nullable = false, unique = true)
    private String statusName; // CREATED, CONFIRMED, CANCELLED
}

