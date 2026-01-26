package com.goBhutan.adminPanel.hotel.controller;

import java.util.List;
import com.goBhutan.adminPanel.common.dto.ApiResponse;
import com.goBhutan.adminPanel.common.exception.ResourceNotFoundException;
import com.goBhutan.adminPanel.hotel.dto.HotelDTO;
import com.goBhutan.adminPanel.hotel.dto.HotelResponseDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import com.goBhutan.adminPanel.hotel.entity.Hotel;
import com.goBhutan.adminPanel.hotel.service.HotelService;
import org.springframework.web.multipart.MultipartFile;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/api/v1/hotels")
public class HotelController {

    @Autowired private HotelService hotelService;

    @GetMapping
   // @PreAuthorize("hasRole('client_admin_hotel')")
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

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ApiResponse<HotelResponseDTO>> createHotel(
          @ModelAttribute @Valid HotelDTO hotelDTO,
          @RequestPart(required = false, name = "hotelImages") List<MultipartFile> hotelImages
  ) {
      try {
          Jwt principal = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
          String adminUserId = principal.getSubject();
          hotelDTO.setAdminUserId(adminUserId);

          HotelResponseDTO createdHotel = hotelService.createHotel(hotelDTO, hotelImages);

          ApiResponse<HotelResponseDTO> response = ApiResponse.<HotelResponseDTO>builder()
                  .success(true)
                  .message("Hotel created successfully")
                  .data(createdHotel)
                  .build();

          return ResponseEntity.status(HttpStatus.CREATED).body(response);
      } catch (Exception e) {
          ApiResponse<HotelResponseDTO> errorResponse = ApiResponse.<HotelResponseDTO>builder()
                  .success(false)
                  .message("Failed to create hotel: " + e.getMessage())
                  .data(null)
                  .build();

          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
      }
  }

    @PutMapping(value = "/updateHotel/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<HotelResponseDTO>> updateHotel(
            @PathVariable Long id,
            @ModelAttribute @Valid HotelDTO hotelDTO,
            @RequestPart(required = false, name = "hotelImages") List<MultipartFile> hotelImages,
            @RequestParam(required = false, name = "deleteImageIds") List<Long> deleteImageIds
    ) {
        try {

            Jwt principal = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String adminUserId = principal.getSubject();
            hotelDTO.setAdminUserId(adminUserId);

            HotelResponseDTO updatedHotel = hotelService.updateHotel(id, hotelDTO, hotelImages, deleteImageIds);

            ApiResponse<HotelResponseDTO> response = ApiResponse.<HotelResponseDTO>builder()
                    .success(true)
                    .message("Hotel updated successfully")
                    .data(updatedHotel)
                    .build();

            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            ApiResponse<HotelResponseDTO> errorResponse = ApiResponse.<HotelResponseDTO>builder()
                    .success(false)
                    .message(e.getMessage())
                    .data(null)
                    .build();

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            ApiResponse<HotelResponseDTO> errorResponse = ApiResponse.<HotelResponseDTO>builder()
                    .success(false)
                    .message("Failed to update hotel: " + e.getMessage())
                    .data(null)
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @DeleteMapping("/{id}")
 //   @PreAuthorize("hasRole('client_admin_hotel')")
    public void deleteHotel(@PathVariable Long id) {
        hotelService.deleteHotel(id);
    }
}