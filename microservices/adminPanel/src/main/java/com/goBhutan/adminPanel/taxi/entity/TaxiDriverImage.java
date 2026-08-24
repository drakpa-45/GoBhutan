package com.goBhutan.adminPanel.taxi.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * One row per vehicle image.
 * A driver can upload as many images as they want.
 * Images are ordered by displayOrder for consistent rendering in the app.
 */
@Entity
@Table(name = "tbl_taxi_driver_image",
       indexes = @Index(name = "idx_tdi_driver", columnList = "taxi_driver_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TaxiDriverImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "taxi_driver_id", nullable = false)
    private TaxiDriver taxiDriver;

    /** Relative path served via /uploads/** e.g. /uploads/taxi/101_0_uuid.jpg */
    @Column(name = "image_path", nullable = false)
    private String imagePath;

    /** Original filename the driver uploaded — useful for display */
    @Column(name = "original_filename")
    private String originalFilename;

    /** 0-based order for rendering images in sequence in the app */
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @PrePersist
    public void onCreate() {
        this.uploadedAt = LocalDateTime.now();
    }
}
