package com.goBhutan.adminPanel.hotel.controller;

import com.goBhutan.adminPanel.common.dto.ApiResponse;
import com.goBhutan.adminPanel.hotel.dto.HotelSearchRequestDTO;
import com.goBhutan.adminPanel.hotel.dto.HotelSearchResultDTO;
import com.goBhutan.adminPanel.hotel.entity.Amenity;
import com.goBhutan.adminPanel.hotel.service.HotelSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/search/hotels")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Public Hotel Search", description = "Public hotel search — no auth required")
public class HotelSearchController {

    private final HotelSearchService hotelSearchService;

    /**
     * GET /api/search/hotels
     * GET /api/search/hotels?keyword=grand&city=Thimphu
     * GET /api/search/hotels?minStars=3&sortBy=STAR_RATING&sortDirection=DESC
     * GET /api/search/hotels?amenityCategories=WELLNESS,DINING&minPrice=500&maxPrice=5000
     */
    @GetMapping
    @Operation(summary = "Search hotels by keyword, location, stars, price, amenities")
    public ResponseEntity<ApiResponse<Page<HotelSearchResultDTO>>> searchHotels(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) Integer minStars,
            @RequestParam(required = false) Integer maxStars,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) List<String> amenityNames,
            @RequestParam(required = false) List<Amenity.AmenityCategory> amenityCategories,
            @RequestParam(required = false) Integer guests,
            @RequestParam(required = false)
            @org.springframework.format.annotation.DateTimeFormat(
                    iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            java.time.LocalDate checkIn,
            @RequestParam(required = false)
            @org.springframework.format.annotation.DateTimeFormat(
                    iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            java.time.LocalDate checkOut,
            @RequestParam(defaultValue = "NEWEST") HotelSearchRequestDTO.SortOption sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        try {
            HotelSearchRequestDTO req = new HotelSearchRequestDTO();
            req.setKeyword(keyword);
            req.setCity(city);
            req.setState(state);
            req.setCountry(country);
            req.setMinStars(minStars);
            req.setMaxStars(maxStars);
            req.setMinPrice(minPrice);
            req.setMaxPrice(maxPrice);
            req.setAmenityNames(amenityNames);
            req.setAmenityCategories(amenityCategories);
            req.setGuests(guests);
            req.setCheckIn(checkIn);
            req.setCheckOut(checkOut);
            req.setSortBy(sortBy);
            req.setSortDirection(sortDirection);
            req.setPage(page);
            req.setSize(size);

            Page<HotelSearchResultDTO> results = hotelSearchService.searchHotels(req);

            String message = results.getTotalElements() == 0
                    ? "No hotels found matching your search"
                    : results.getTotalElements() + " hotel(s) found";

            return ResponseEntity.ok(ApiResponse.success(message, results));

        } catch (Exception e) {
            log.error("Error searching hotels: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to search hotels: " + e.getMessage()));
        }
    }

    // POST variant for complex filter screens on mobile
    @PostMapping
    @Operation(summary = "Search hotels — full filter body")
    public ResponseEntity<ApiResponse<Page<HotelSearchResultDTO>>> searchHotelsPost(
            @RequestBody HotelSearchRequestDTO req
    ) {
        try {
            Page<HotelSearchResultDTO> results = hotelSearchService.searchHotels(req);

            String message = results.getTotalElements() == 0
                    ? "No hotels found matching your search"
                    : results.getTotalElements() + " hotel(s) found";

            return ResponseEntity.ok(ApiResponse.success(message, results));

        } catch (Exception e) {
            log.error("Error searching hotels: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to search hotels: " + e.getMessage()));
        }
    }
}