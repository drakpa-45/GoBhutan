package com.goBhutan.hotel.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.goBhutan.hotel.entity.Hotel;
import com.goBhutan.hotel.repository.HotelRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class HotelService {
    
    @Autowired
    private HotelRepository hotelRepository;
    
    public List<Hotel> getHotelsByAdmin(String adminUserId) {
        return hotelRepository.findByAdminUserIdAndIsActive(adminUserId, true);
    }
    
    public Optional<Hotel> getHotelById(Long id, String adminUserId) {
        return hotelRepository.findByIdAndAdminUserId(id, adminUserId);
    }
    
    public Hotel saveHotel(Hotel hotel, String adminUserId) {
        hotel.setAdminUserId(adminUserId);
        if (hotel.getId() != null) {
            hotel.setUpdatedAt(LocalDateTime.now());
        }
        return hotelRepository.save(hotel);
    }
    
    public void deleteHotel(Long id, String adminUserId) {
        Optional<Hotel> hotel = hotelRepository.findByIdAndAdminUserId(id, adminUserId);
        if (hotel.isPresent()) {
            Hotel h = hotel.get();
            h.setIsActive(false);
            h.setUpdatedAt(LocalDateTime.now());
            hotelRepository.save(h);
        }
    }
    
    public long getActiveHotelCount(String adminUserId) {
        return hotelRepository.countActiveHotelsByAdmin(adminUserId);
    }
}
