package com.goBhutan.adminPanel.theater.dto.theater;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TheaterLocationResponseDTO {

    private Long id;
    private String dzongkhag;
    private String thromdoe;
    private String address;
    private Instant createdAt;
    private Integer theaterCount;
}