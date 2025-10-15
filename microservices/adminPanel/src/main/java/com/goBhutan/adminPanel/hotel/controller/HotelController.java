package com.goBhutan.adminPanel.hotel.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.goBhutan.adminPanel.common.dto.ApiResponse;
import com.goBhutan.adminPanel.hotel.dto.HotelMultipartRequest;
import com.goBhutan.adminPanel.hotel.dto.HotelResponseDTO;
import com.goBhutan.adminPanel.hotel.dto.RoomImageUploadDTO;
import com.goBhutan.adminPanel.hotel.mapper.HotelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

  /*  @PostMapping
// @PreAuthorize("hasRole('client_admin_hotel')")
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
    }*/
   // @PreAuthorize("hasRole('client_admin_hotel')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<HotelResponseDTO> createHotel(
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam String address,
            @RequestParam String city,
            @RequestParam String state,
            @RequestParam String country,
            @RequestParam String postalCode,
            @RequestParam String phoneNumber,
            @RequestParam String email,
            @RequestParam(required = false) String website,
            @RequestParam(required = false) Integer starRating,
            @RequestPart(required = false) List<MultipartFile> hotelImages,
            @RequestPart(required = false) List<RoomImageUploadDTO> roomImages
    ) {
        Hotel hotel = new Hotel();
        hotel.setName(name);
        hotel.setDescription(description);
        hotel.setAddress(address);
        hotel.setCity(city);
        hotel.setState(state);
        hotel.setCountry(country);
        hotel.setPostalCode(postalCode);
        hotel.setPhoneNumber(phoneNumber);
        hotel.setEmail(email);
        hotel.setWebsite(website);
        hotel.setStarRating(starRating);

        Map<Long, List<MultipartFile>> roomImagesMap = roomImages.stream()
                .collect(Collectors.toMap(RoomImageUploadDTO::getRoomId, RoomImageUploadDTO::getImages));

        Hotel createdHotel = hotelService.createHotel(hotel, hotelImages, roomImagesMap);
        return ApiResponse.success("Hotel created successfully", HotelMapper.toHotelResponseDTO(createdHotel));
    }

//    @PostMapping(consumes={MediaType.MULTIPART_FORM_DATA_VALUE},produces = {MediaType.APPLICATION_JSON_VALUE})
//    public ApiResponse<HotelResponseDTO> createHotel(
//            @ModelAttribute HotelMultipartRequest request
//    ) {
//        // Convert RoomImageUploadDTO list to a Map<Long, List<MultipartFile>> for your service
//        Map<Long, List<MultipartFile>> roomImagesMap = request.getRoomImages().stream()
//                .collect(Collectors.toMap(RoomImageUploadDTO::getRoomId, RoomImageUploadDTO::getImages));
//
//        Hotel createdHotel = hotelService.createHotel(
//                request.getHotel(),
//                request.getHotelImages(),
//                roomImagesMap
//        );
//
//        HotelResponseDTO hotelDTO = HotelMapper.toHotelResponseDTO(createdHotel);
//        return ApiResponse.success("Hotel created successfully", hotelDTO);
//    }
    //@PreAuthorize("hasRole('client_admin_hotel')")
    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<ApiResponse<HotelResponseDTO>> updateHotel(
            @PathVariable Long id,
            @ModelAttribute HotelMultipartRequest request
    ) {
        // Convert RoomImageUploadDTO list to Map<Long, List<MultipartFile>>
        Map<Long, List<MultipartFile>> roomImagesMap = new HashMap<>();
        if (request.getRoomImages() != null) {
            roomImagesMap = request.getRoomImages().stream()
                    .collect(Collectors.toMap(
                            RoomImageUploadDTO::getRoomId,
                            RoomImageUploadDTO::getImages
                    ));
        }

        HotelResponseDTO updatedHotel = hotelService.updateHotel(
                id,
                request.getHotel(),
                request.getHotelImages(),
                roomImagesMap
        );

        return ResponseEntity.ok(
                ApiResponse.success("Hotel updated successfully", updatedHotel)
        );
    }

    @DeleteMapping("/{id}")
 //   @PreAuthorize("hasRole('client_admin_hotel')")
    public void deleteHotel(@PathVariable Long id) {
        hotelService.deleteHotel(id);
    }
}