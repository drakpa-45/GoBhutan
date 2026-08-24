package com.goBhutan.adminPanel.taxi.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tbl_taxi_driver",
       uniqueConstraints = {
           @UniqueConstraint(name = "uq_license_no",     columnNames = "license_number"),
           @UniqueConstraint(name = "uq_vehicle_reg_no", columnNames = "registration_number")
       })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TaxiDriver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "driver_id", nullable = false, unique = true)
    private String driverId;

    // ── Vehicle ───────────────────────────────────────────────────────────────

    @Column(name = "vehicle_make", nullable = false, length = 100)
    private String vehicleMake;

    @Column(name = "vehicle_model", nullable = false, length = 100)
    private String vehicleModel;

    @Column(name = "vehicle_color", nullable = false, length = 50)
    private String vehicleColor;

    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats;

    @Column(name = "phone_number", nullable = false)
    private Integer phoneNumber;

    @Column(name = "driver_name", nullable = false, length = 50)
    private String driverName;

    // ── Documents ─────────────────────────────────────────────────────────────

    @Column(name = "license_number", nullable = false, unique = true, length = 50)
    private String licenseNumber;

    @Column(name = "registration_number", nullable = false, unique = true, length = 50)
    private String registrationNumber;

    // ── Images (unlimited, ordered) ───────────────────────────────────────────

    @OneToMany(mappedBy = "taxiDriver",
               cascade = CascadeType.ALL,
               orphanRemoval = true,
               fetch = FetchType.LAZY)
    @OrderBy("displayOrder ASC")
    @Builder.Default
    private List<TaxiDriverImage> images = new ArrayList<>();

    // ── Availability ──────────────────────────────────────────────────────────

    @Column(name = "is_online", nullable = false)
    private Boolean isOnline = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.isOnline == null) this.isOnline = false;
    }
}
