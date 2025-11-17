package com.goBhutan.adminPanel.busAdmin.dto;

import com.goBhutan.adminPanel.busAdmin.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingResponseDTO {
    private Long id;
    private String bookingReference;
    private String passengerName;
    private String email;
    private String phone;
    private BigDecimal totalFare;
    private BookingStatus status;
    private LocalDateTime bookingDate;
    private LocalDateTime cancelledAt;
    private String cancellationReason;
    private Long scheduleId;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private String source;
    private String destination;
    private String busNumber;
    private String busType;

    private List<SeatDetailsDTO> seats;
}
