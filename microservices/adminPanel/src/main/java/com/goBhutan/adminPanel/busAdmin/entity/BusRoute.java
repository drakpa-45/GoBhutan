package com.goBhutan.adminPanel.busAdmin.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "tbl_bs_bus_routes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BusRoute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ---------------- BUS ----------------
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bus_id", nullable = false)
    @JsonIgnore
    private Bus bus;

    // ---------------- BASIC ROUTE INFO ----------------
    @NotBlank(message = "Source is required")
    @Column(nullable = false)
    private String source;

    @NotBlank(message = "Destination is required")
    @Column(nullable = false)
    private String destination;

    @NotNull(message = "Distance is required")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal distance;

    @NotNull(message = "Base fare is required")
    @DecimalMin(value = "0.01", message = "Base fare must be greater than 0")
    @Column(name = "base_fare", nullable = false, precision = 10, scale = 2)
    private BigDecimal baseFare;

    // Duration in minutes.
    @Column(name = "estimated_duration_minutes")
    private Integer estimatedDurationMinutes;

    // ---------------- DEPARTURE & FARE ----------------
    @NotNull(message = "Departure time is required")
    @Column(name = "departure_time", nullable = false)
    @JsonFormat(pattern = "HH:mm")
    private LocalTime departureTime;

    @Column(name = "check_in_time")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime checkInTime;

    // Additional app charge added on top of base fare.
    @DecimalMin(value = "0.0", message = "App charges must be zero or greater")
    @Column(name = "app_charges", precision = 10, scale = 2)
    private BigDecimal appCharges;

    // ---------------- STATUS ----------------
    @Column(nullable = false)
    private Boolean active = true;

    // ---------------- AUDIT ----------------
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Utility getter
    public BigDecimal getFinalFare() {
        if (baseFare == null) {
            return null;
        }
        return baseFare.add(getAppCharges() != null ? getAppCharges() : BigDecimal.ZERO);
    }

    public BigDecimal getAppCharges() {
        return appCharges != null ? appCharges : BigDecimal.ZERO;
    }
}
