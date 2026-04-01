package com.goBhutan.adminPanel.theater.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tbl_mvth_seat_class")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name; // e.g., REGULAR, PREMIUM, VIP

    @Column(name = "description")
    private String description; // optional

    @Column(name = "default_base_price")
    private Double defaultBasePrice; // optional system-suggested price
}
