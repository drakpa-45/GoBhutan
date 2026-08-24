package com.goBhutan.adminPanel.taxi.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "tbl_master_drop_point")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DropPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;                    // e.g. "Chuzom Junction"

    @Column(name = "dzongkhag", length = 100)
    private String dzongkhag;              // which district it belongs to

    @Column(name = "latitude", nullable = false, precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(name = "longitude", nullable = false, precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "dropPointId", length = 11)
    private Long dropPointId;


}