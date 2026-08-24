package com.goBhutan.adminPanel.theater.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tbl_mvth_theater_locations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TheaterLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dzongkhag", nullable = false)
    private String dzongkhag;

    @Column(name = "thromdoe")
    private String thromdoe;

    @Column(name = "address")
    private String address;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Theater> theaters = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    // Helper method
    public String getName() {
        StringBuilder name = new StringBuilder(dzongkhag);
        if (thromdoe != null && !thromdoe.isEmpty()) {
            name.append(", ").append(thromdoe);
        }
        return name.toString();
    }
}