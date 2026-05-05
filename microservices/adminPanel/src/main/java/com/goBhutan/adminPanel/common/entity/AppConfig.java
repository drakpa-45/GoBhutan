package com.goBhutan.adminPanel.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_config")
@Getter
@Setter
public class AppConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "config_id")
    private Long configId;

    @Column(name = "CONFIG_FOR", nullable = false, length = 120)
    private String configFor;

    @Column(name = "CONFIG_VALUE", nullable = false, columnDefinition = "TEXT")
    private String configValue;

    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    @Column(name = "admin_user_id", nullable = false, length = 120)
    private String adminUserId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
