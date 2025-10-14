package com.goBhutan.adminPanel.hotel.repository;

import com.goBhutan.adminPanel.hotel.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    List<Image> findByHotelId(Long hotelId);
    List<Image> findByRoomId(Long roomId);
}
