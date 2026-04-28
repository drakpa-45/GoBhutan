package com.goBhutan.adminPanel.hotel.controller;

import com.goBhutan.adminPanel.common.dto.ApiResponse;
import com.goBhutan.adminPanel.hotel.dto.HotelSearchRequestDTO;
import com.goBhutan.adminPanel.hotel.dto.HotelSearchResultDTO;
import com.goBhutan.adminPanel.hotel.service.HotelSearchService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hotels")
@CrossOrigin(origins = "*", maxAge = 3600)
public class HotelSearchController {

    @Autowired
    private HotelSearchService hotelSearchService;

    /**
     * POST /hotels/search
     * Mobile-friendly search: body-based so filters are easily extended.
     */
    @PostMapping("/search")
    public ApiResponse<Page<HotelSearchResultDTO>> searchHotels(
            @RequestBody @Valid HotelSearchRequestDTO request) {

        Page<HotelSearchResultDTO> results = hotelSearchService.search(request);
        return new ApiResponse<>(true, "Hotels fetched successfully", results);
    }

    /**
     * GET /hotels/search?city=Thimphu&minRating=4&sortBy=POPULARITY
     * Optional: lightweight GET variant for simple screens (shareable links, etc.)
     */
    @GetMapping("/search")
    public ApiResponse<Page<HotelSearchResultDTO>> searchHotelsGet(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Integer minStars,
            @RequestParam(required = false) Integer maxStars,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) Double radiusKm,
            @RequestParam(defaultValue = "POPULARITY") HotelSearchRequestDTO.SortOption sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        HotelSearchRequestDTO request = new HotelSearchRequestDTO();
        request.setKeyword(keyword);
        request.setCity(city);
        request.setMinRating(minRating);
        request.setMinStars(minStars);
        request.setMaxStars(maxStars);
        request.setLatitude(latitude);
        request.setLongitude(longitude);
        request.setRadiusKm(radiusKm);
        request.setSortBy(sortBy);
        request.setSortDirection(sortDirection);
        request.setPage(page);
        request.setSize(size);

        Page<HotelSearchResultDTO> results = hotelSearchService.search(request);
        return new ApiResponse<>(true, "Hotels fetched successfully", results);
    }
}