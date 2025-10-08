package com.goBhutan.adminPanel.hotel.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.goBhutan.adminPanel.hotel.dto.HotelResponseDTO;
import com.goBhutan.adminPanel.hotel.entity.Room;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
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

    public List<HotelResponseDTO> getAllHotelsForCurrentAdmin() {
        // 🔹 Extract Keycloak userId from token
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId;

        if (principal instanceof Jwt jwt) {
            userId = jwt.getSubject(); // Keycloak "sub" claim
        } else if (principal instanceof String str) {
            userId = str; // fallback if principal is String
        } else {
            throw new RuntimeException("Unsupported principal type: " + principal.getClass());
        }

        List<Hotel> hotels = hotelRepository.findByAdminUserId(userId);
        return hotels.stream()
                .map(hotel ->
                        new HotelResponseDTO(
                        hotel.getId(),
                        hotel.getName(),
                        hotel.getDescription(),
                        hotel.getAddress(),
                        hotel.getCity(),
                        hotel.getState(),
                        hotel.getCountry(),
                        hotel.getPostalCode(),
                        hotel.getPhoneNumber(),
                        hotel.getEmail(),
                        hotel.getWebsite(),
                        hotel.getStarRating(),
                        hotel.getIsActive(),
                        hotel.getCreatedAt(),
                        hotel.getUpdatedAt()
                ))
                .collect(Collectors.toList());
    }


    public Hotel getHotelById(Long id) {
        return hotelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hotel not found"));
    }

    public Hotel createHotel(Hotel hotel) {
        hotel.setCreatedAt(LocalDateTime.now());
        hotel.setUpdatedAt(LocalDateTime.now());
        // 🔹 Extract Keycloak userId from token
        Jwt principal = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = principal.getSubject(); // Keycloak "sub" claim
        hotel.setAdminUserId(userId);
        // Set hotel reference in rooms
        if (hotel.getRooms() != null) {
            for (Room room : hotel.getRooms()) {
                room.setHotel(hotel);
            }
        }
        return hotelRepository.save(hotel);
    }

    public HotelResponseDTO updateHotel(Long id, Hotel updated) {
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
     //   return hotelRepository.save(hotel);
        Hotel savedHotel =hotelRepository.save(hotel);

        return new HotelResponseDTO(
                savedHotel.getId(),
                savedHotel.getName(),
                savedHotel.getDescription(),
                savedHotel.getAddress(),
                savedHotel.getCity(),
                savedHotel.getState(),
                savedHotel.getCountry(),
                savedHotel.getPostalCode(),
                savedHotel.getPhoneNumber(),
                savedHotel.getEmail(),
                savedHotel.getWebsite(),
                savedHotel.getStarRating(),
                savedHotel.getIsActive(),
                savedHotel.getCreatedAt(),
                savedHotel.getUpdatedAt()
        );
    }

    public void deleteHotel(Long id) {
        hotelRepository.deleteById(id);
    }
}
