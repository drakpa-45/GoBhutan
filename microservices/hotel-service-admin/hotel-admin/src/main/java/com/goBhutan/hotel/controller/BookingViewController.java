package com.goBhutan.hotel.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.goBhutan.hotel.service.BookingService;
import com.goBhutan.hotel.service.HotelService;

@Controller
@RequestMapping("/bookings")
public class BookingViewController {
    
    @Autowired
    private BookingService bookingService;
    
    @Autowired
    private HotelService hotelService;
    
    @GetMapping
    public String listBookings(@RequestParam(required = false) Long hotelId, 
                              Model model, @AuthenticationPrincipal Jwt jwt) {
    	String adminUserId = jwt.getSubject(); // Keycloak user ID
        var hotels = hotelService.getHotelsByAdmin(adminUserId);
        model.addAttribute("hotels", hotels);
        
        if (hotelId != null) {
            var bookings = bookingService.getBookingsByHotel(hotelId);
            model.addAttribute("bookings", bookings);
            model.addAttribute("selectedHotelId", hotelId);
            model.addAttribute("bookingStatusCounts", bookingService.getBookingStatusCounts(hotelId));
        } else {
            var allBookings = bookingService.getBookingsByAdmin(adminUserId);
            model.addAttribute("bookings", allBookings);
        }
        
        return "booking/booking-overview";
    }
    
}