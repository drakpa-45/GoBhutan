package com.goBhutan.adminPanel.hotel.service;

import com.goBhutan.adminPanel.hotel.dto.HotelSearchRequestDTO;
import com.goBhutan.adminPanel.hotel.dto.HotelSearchResultDTO;
import com.goBhutan.adminPanel.hotel.repository.HotelSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HotelSearchService {

    private final HotelSearchRepository hotelSearchRepository;

    public Page<HotelSearchResultDTO> searchHotels(HotelSearchRequestDTO req) {
        Pageable pageable = PageRequest.of(
                req.getPage(),
                req.getSize()
                // No Sort here — sorting handled inside repository via Criteria API
        );
        return hotelSearchRepository.searchHotels(req, pageable);
    }
}