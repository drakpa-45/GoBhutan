package com.goBhutan.hotel.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.goBhutan.hotel.entity.RoomType;
import com.goBhutan.hotel.repository.RoomTypeRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class RoomTypeService {
    
    @Autowired
    private RoomTypeRepository roomTypeRepository;
    
    public List<RoomType> getAllRoomTypes() {
        return roomTypeRepository.findAll();
    }
    
    public RoomType saveRoomType(RoomType roomType) {
        return roomTypeRepository.save(roomType);
    }
    
    public void deleteRoomType(Long id) {
        roomTypeRepository.deleteById(id);
    }
}
