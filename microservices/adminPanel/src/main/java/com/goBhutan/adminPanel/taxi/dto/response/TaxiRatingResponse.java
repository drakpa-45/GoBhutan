package com.goBhutan.adminPanel.taxi.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class TaxiRatingResponse {
    private Long          id;
    private Long          bookingId;
    private String        driverId;
    private Integer       rating;
    private String        comment;
    private LocalDateTime createdAt;
}