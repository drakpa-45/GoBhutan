package com.goBhutan.adminPanel.hotel.controller;

import com.goBhutan.adminPanel.hotel.entity.Booking;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.goBhutan.adminPanel.hotel.service.BookingService;

import java.util.List;

@Controller
@RequestMapping("/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @GetMapping
    @PreAuthorize("hasRole('client_admin')")
    public List<Booking> getAllBookings() {
        return bookingService.getAllBookings();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('client_admin')")
    public Booking getBooking(@PathVariable Long id) {
        return bookingService.getBooking(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('client_admin')")
    public Booking createBooking(
            @RequestParam Long hotelId,
            @RequestParam Long roomId,
            @RequestBody @Valid Booking booking) {
        return bookingService.createBooking(hotelId, roomId, booking);
    }

    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasRole('client_admin')")
    public Booking confirmBooking(@PathVariable Long id) {
        return bookingService.confirmBooking(id);
    }

    @PutMapping("/{id}/checkin")
    @PreAuthorize("hasRole('client_admin')")
    public Booking checkIn(@PathVariable Long id) {
        return bookingService.checkIn(id);
    }

    @PutMapping("/{id}/checkout")
    @PreAuthorize("hasRole('client_admin')")
    public Booking checkOut(@PathVariable Long id) {
        return bookingService.checkOut(id);
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('client_admin')")
    public Booking cancelBooking(@PathVariable Long id) {
        return bookingService.cancelBooking(id);
    }
}