package com.goBhutan.adminPanel.hotel.repository;

import com.goBhutan.adminPanel.hotel.entity.HotelImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<HotelImage, Long> {
    List<HotelImage> findByHotelId(Long hotelId);
    List<HotelImage> findByRoomId(Long roomId);
}
