package com.goBhutan.adminPanel.busAdmin.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "tbl_bs_route_master",
        uniqueConstraints = @UniqueConstraint(columnNames = {"admin_user_id", "route_name"})
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BusRouteMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Route name is required")
    @Column(name = "route_name", nullable = false)
    private String routeName;

    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    @Column(name = "admin_user_id", nullable = false)
    private String adminUserId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
