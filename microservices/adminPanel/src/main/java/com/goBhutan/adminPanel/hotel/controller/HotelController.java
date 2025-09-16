package com.goBhutan.adminPanel.hotel.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.goBhutan.adminPanel.hotel.entity.Hotel;
import com.goBhutan.adminPanel.hotel.service.HotelService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/hotels")
public class HotelController {

    @Autowired private HotelService hotelService;

    @GetMapping
    @PreAuthorize("hasRole('client_admin')")
    public List<Hotel> getAllHotels() {
        return hotelService.getAllHotels();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('client_admin')")
    public Hotel getHotel(@PathVariable Long id) {
        return hotelService.getHotelById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('client_admin')")
    public Hotel createHotel(@RequestBody @Valid Hotel hotel) {
        return hotelService.createHotel(hotel);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('client_admin')")
    public Hotel updateHotel(@PathVariable Long id, @RequestBody Hotel hotel) {
        return hotelService.updateHotel(id, hotel);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('client_admin')")
    public void deleteHotel(@PathVariable Long id) {
        hotelService.deleteHotel(id);
    }
}