package com.goBhutan.adminPanel.theater.dto.theater;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TheaterResponseDTO {

    private Long id;
    private String name;
    private String description;
    private Long locationId;
    private String locationName;
    private String adminUserId;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
    private Integer hallCount;
}