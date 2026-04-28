package com.goBhutan.adminPanel.hotel.controller;

import com.goBhutan.adminPanel.common.dto.ApiResponse;
import com.goBhutan.adminPanel.hotel.dto.BookingRequestDTO;
import com.goBhutan.adminPanel.hotel.dto.BookingSummaryDTO;
import com.goBhutan.adminPanel.hotel.entity.Booking;
import com.goBhutan.adminPanel.hotel.repository.BookingSummary;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.goBhutan.adminPanel.hotel.service.BookingService;

import java.util.List;
@RestController
@RequestMapping("/bookings")
@CrossOrigin(origins = "*", maxAge = 3600)
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @GetMapping("/hotel/{hotelId}")
// @PreAuthorize("hasRole('client_admin')")
    public ApiResponse<List<BookingSummary>> getBookingsByHotel(@PathVariable Long hotelId) {
        List<BookingSummary> bookings = bookingService.getBookingSummariesByHotel(hotelId);
        return new ApiResponse<>(true, "Bookings fetched successfully", bookings);
    }

    @GetMapping("/{id}")
   // @PreAuthorize("hasRole('client_admin')")
    public ApiResponse<Booking> getBooking(@PathVariable Long id) {
        return new ApiResponse<>(true, "Booking fetched successfully", bookingService.getBooking(id));
    }

    @GetMapping("/hotel/count")
    // @PreAuthorize("hasRole('client_admin')")
    public ApiResponse<Long> getTotalBookingCount() {
        Long count = bookingService.getTotalBookingCountByHotel();
        return new ApiResponse<>(true, "Total booking count fetched successfully", count);
    }

    @PostMapping
   // @PreAuthorize("hasRole('client_admin')")
    public ApiResponse<Booking> createBooking(@RequestBody @Valid BookingRequestDTO bookingRequest) {
        Booking created = bookingService.createBooking(bookingRequest);
        return new ApiResponse<>(true, "Booking created successfully", created);
    }

    @PutMapping("/{id}/confirm/{bookingReference}")
// @PreAuthorize("hasRole('client_admin')")
    public ApiResponse<Void> confirmBooking(@PathVariable Long id, @PathVariable String bookingReference) {
        bookingService.confirmBooking(id,bookingReference);
        return new ApiResponse<>(true, "Booking confirmed successfully", null);
    }

    @PutMapping("/{id}/checkin")
   // @PreAuthorize("hasRole('client_admin')")
    public ApiResponse<Void> checkIn(@PathVariable Long id) {
        bookingService.checkIn(id);
        return new ApiResponse<>(true, "Check-in successful", null);
    }

    @PutMapping("/{id}/checkout")
 //   @PreAuthorize("hasRole('client_admin')")
    public ApiResponse<Void> checkOut(@PathVariable Long id) {
        bookingService.checkOut(id);
        return new ApiResponse<>(true, "Check-out successful", null);
    }

    @PutMapping("/{id}/cancel")
   // @PreAuthorize("hasRole('client_admin')")
    public ApiResponse<Void> cancelBooking(@PathVariable Long id) {
        bookingService.cancelBooking(id);
        return new ApiResponse<>(true, "Booking cancelled",null);
    }
}
