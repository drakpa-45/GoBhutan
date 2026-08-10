package com.goBhutan.adminPanel.taxi.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "tbl_master_dzongkhag")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dzongkhag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;                    // e.g. "Thimphu"

    @Column(name = "latitude", nullable = false, precision = 10, scale = 7)
    private BigDecimal latitude;            // center coordinate

    @Column(name = "longitude", nullable = false, precision = 10, scale = 7)
    private BigDecimal longitude;
}