package com.goBhutan.adminPanel.hotel.repository;

import com.goBhutan.adminPanel.hotel.dto.HotelSearchRequestDTO;
import com.goBhutan.adminPanel.hotel.dto.HotelSearchResultDTO;
import com.goBhutan.adminPanel.hotel.entity.Hotel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface HotelSearchRepository extends JpaRepository<Hotel, Long>,
        HotelSearchRepositoryCustom {
}

// ---- Custom interface ----
interface HotelSearchRepositoryCustom {
    Page<HotelSearchResultDTO> searchHotels(HotelSearchRequestDTO request, Pageable pageable);
}