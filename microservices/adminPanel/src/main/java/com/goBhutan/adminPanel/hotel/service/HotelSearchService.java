package com.goBhutan.adminPanel.hotel.service;

import com.goBhutan.adminPanel.hotel.dto.HotelSearchRequestDTO;
import com.goBhutan.adminPanel.hotel.dto.HotelSearchResultDTO;
import com.goBhutan.adminPanel.hotel.repository.HotelSearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class HotelSearchService {

    @Autowired
    private HotelSearchRepository hotelSearchRepository;

    public Page<HotelSearchResultDTO> search(HotelSearchRequestDTO req) {
        Sort sort = buildSort(req);
        Pageable pageable = PageRequest.of(req.getPage(), req.getSize(), sort);

        return hotelSearchRepository.searchHotels(req, pageable);
    }

    private Sort buildSort(HotelSearchRequestDTO req) {
        boolean asc = "ASC".equalsIgnoreCase(req.getSortDirection());
        return switch (req.getSortBy()) {
            case RATING         -> Sort.by(asc ? Sort.Order.asc("averageRating")   : Sort.Order.desc("averageRating"));
            case PRICE_LOW_HIGH -> Sort.by(Sort.Order.asc("pricePerNight"));
            case PRICE_HIGH_LOW -> Sort.by(Sort.Order.desc("pricePerNight"));
            case NEWEST         -> Sort.by(Sort.Order.desc("createdAt"));
            case DISTANCE       -> Sort.unsorted(); // handled in JPQL/native query with Haversine
            default             -> Sort.by(Sort.Order.desc("totalBookings")); // POPULARITY
        };
    }
}