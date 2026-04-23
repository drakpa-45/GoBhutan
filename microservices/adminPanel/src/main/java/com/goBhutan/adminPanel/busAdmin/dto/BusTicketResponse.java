package com.goBhutan.adminPanel.busAdmin.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BusTicketResponse {
    private Long bookingId;
    private String bookingRef;
    private String paymentRef;
    private Long scheduleId;
    private String busNumber;
    private String busName;
    private String source;
    private String destination;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private Integer seatNumber;
    private String seatLabel;
    private String applicantCid;
    private String applicantMobile;
    private String applicantEmail;
    private String status;
    private String qrCodeBase64;
}
