package com.goBhutan.adminPanel.hotel.dto;

import com.goBhutan.adminPanel.hotel.entity.Booking.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingWithHotelDTO {
    private String hotelName;
    private BookingStatus status;
    private Long totalBookingCount;
}