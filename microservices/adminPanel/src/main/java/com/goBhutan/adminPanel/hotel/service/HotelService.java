package com.goBhutan.adminPanel.hotel.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.goBhutan.adminPanel.hotel.entity.Hotel;
import com.goBhutan.adminPanel.hotel.repository.HotelRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class HotelService {

    @Autowired
    private HotelRepository hotelRepository;

    public List<Hotel> getAllHotels() {
        return hotelRepository.findAll();
    }

    public Hotel getHotelById(Long id) {
        return hotelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hotel not found"));
    }

    public Hotel createHotel(Hotel hotel) {
        hotel.setCreatedAt(LocalDateTime.now());
        hotel.setUpdatedAt(LocalDateTime.now());
        return hotelRepository.save(hotel);
    }

    public Hotel updateHotel(Long id, Hotel updated) {
        Hotel hotel = getHotelById(id);
        hotel.setName(updated.getName());
        hotel.setDescription(updated.getDescription());
        hotel.setAddress(updated.getAddress());
        hotel.setCity(updated.getCity());
        hotel.setState(updated.getState());
        hotel.setCountry(updated.getCountry());
        hotel.setPostalCode(updated.getPostalCode());
        hotel.setPhoneNumber(updated.getPhoneNumber());
        hotel.setEmail(updated.getEmail());
        hotel.setWebsite(updated.getWebsite());
        hotel.setStarRating(updated.getStarRating());
        hotel.setUpdatedAt(LocalDateTime.now());
        return hotelRepository.save(hotel);
    }

    public void deleteHotel(Long id) {
        hotelRepository.deleteById(id);
    }
}
