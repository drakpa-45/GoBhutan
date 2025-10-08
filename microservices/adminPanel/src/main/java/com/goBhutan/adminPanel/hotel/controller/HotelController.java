package com.goBhutan.adminPanel.hotel.controller;

import java.util.List;

import com.goBhutan.adminPanel.common.dto.ApiResponse;
import com.goBhutan.adminPanel.hotel.dto.HotelResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.goBhutan.adminPanel.hotel.entity.Hotel;
import com.goBhutan.adminPanel.hotel.service.HotelService;

import jakarta.validation.Valid;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/api/v1/hotels")
public class HotelController {

    @Autowired private HotelService hotelService;

    @GetMapping
   // @PreAuthorize("hasRole('client_admin')")
    public ResponseEntity<ApiResponse<List<HotelResponseDTO>>> getAllHotels() {
        //return hotelService.getAllHotelsForCurrentAdmin();
        List<HotelResponseDTO> hotels = hotelService.getAllHotelsForCurrentAdmin();
        return ResponseEntity.ok(ApiResponse.success("Hotels fetched successfully", hotels));
    }

    @GetMapping("/{id}")
  //  @PreAuthorize("hasRole('client_admin_hotel')")
    public Hotel getHotel(@PathVariable Long id) {
        return hotelService.getHotelById(id);
    }

    @PostMapping
// @PreAuthorize("hasRole('client_admin')")
    public ApiResponse<HotelResponseDTO> createHotel(@RequestBody @Valid Hotel hotel) {
        // Save the hotel
        Hotel createdHotel = hotelService.createHotel(hotel);

        // Map to DTO
        HotelResponseDTO hotelDTO = new HotelResponseDTO(
                createdHotel.getId(),
                createdHotel.getName(),
                createdHotel.getDescription(),
                createdHotel.getAddress(),
                createdHotel.getCity(),
                createdHotel.getState(),
                createdHotel.getCountry(),
                createdHotel.getPostalCode(),
                createdHotel.getPhoneNumber(),
                createdHotel.getEmail(),
                createdHotel.getWebsite(),
                createdHotel.getStarRating(),
                createdHotel.getIsActive(),
                createdHotel.getCreatedAt(),
                createdHotel.getUpdatedAt()
        );

        // Wrap in ApiResponse
        return ApiResponse.success("Hotel created successfully", hotelDTO);
    }
    @PutMapping("/{id}")
   // @PreAuthorize("hasRole('client_admin_hotel')")
    public ResponseEntity<ApiResponse<HotelResponseDTO>> updateHotel(
            @PathVariable Long id,
            @RequestBody Hotel hotel) {

        HotelResponseDTO updatedHotel = hotelService.updateHotel(id, hotel);

        return ResponseEntity.ok(
                ApiResponse.success("Hotel updated successfully", updatedHotel)
        );
    }

    @DeleteMapping("/{id}")
   // @PreAuthorize("hasRole('client_admin_hotel')")
    public void deleteHotel(@PathVariable Long id) {
        hotelService.deleteHotel(id);
    }
}