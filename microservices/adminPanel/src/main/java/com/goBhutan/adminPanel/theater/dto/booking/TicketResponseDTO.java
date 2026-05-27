package com.goBhutan.adminPanel.theater.dto.booking;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponseDTO {
    private String ticketNumber;
    private Long seatId;
    private String seatIdentifier;
    private String seatClass;
    private String customerName;
    private String cidOrPassport;
    private String phoneNumber;
    private String email;
    private Instant bookedAt;
    private Long screeningId;
    private String screeningName;
}