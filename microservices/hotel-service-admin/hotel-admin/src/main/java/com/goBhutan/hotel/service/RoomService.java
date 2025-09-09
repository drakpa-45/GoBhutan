package com.goBhutan.hotel.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.goBhutan.hotel.entity.Room;
import com.goBhutan.hotel.entity.Room.RoomStatus;
import com.goBhutan.hotel.repository.RoomRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class RoomService {
    
    @Autowired
    private RoomRepository roomRepository;
    
    public List<Room> getRoomsByHotel(Long hotelId) {
        return roomRepository.findByHotelIdAndIsActive(hotelId, true);
    }
    
    public Room saveRoom(Room room) {
        if (room.getId() != null) {
            room.setUpdatedAt(LocalDateTime.now());
        }
        return roomRepository.save(room);
    }
    
    public void deleteRoom(Long id) {
        Room room = roomRepository.findById(id).orElse(null);
        if (room != null) {
            room.setIsActive(false);
            room.setUpdatedAt(LocalDateTime.now());
            roomRepository.save(room);
        }
    }
    
    public void updateRoomStatus(Long roomId, RoomStatus status) {
        Room room = roomRepository.findById(roomId).orElse(null);
        if (room != null) {
            room.setStatus(status);
            room.setUpdatedAt(LocalDateTime.now());
            roomRepository.save(room);
        }
    }
    
    public Map<RoomStatus, Long> getRoomStatusCounts(Long hotelId) {
        return java.util.Arrays.stream(RoomStatus.values())
                .collect(Collectors.toMap(
                    status -> status,
                    status -> roomRepository.countRoomsByHotelAndStatus(hotelId, status)
                ));
    }
    
    public List<Room> getRoomsByStatus(Long hotelId, RoomStatus status) {
        return roomRepository.findByHotelIdAndStatus(hotelId, status);
    }
}