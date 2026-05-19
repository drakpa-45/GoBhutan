package com.goBhutan.adminPanel.gasDelivery.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_gs_config_master",uniqueConstraints = @UniqueConstraint(columnNames = {"admin_user_id", "gas_type"})
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GasConfigMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "gas type is required")
    @Column(name = "gas_type", nullable = false)
    private String gasType;

    @Column(name = "quantity")
    private Integer quantity;

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
