package com.goBhutan.adminPanel.hotel.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.goBhutan.adminPanel.hotel.entity.Hotel;
import com.goBhutan.adminPanel.hotel.entity.RoomType;
import com.goBhutan.adminPanel.hotel.repository.HotelRepository;
import com.goBhutan.adminPanel.hotel.repository.RoomTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.goBhutan.adminPanel.hotel.entity.Room;
import com.goBhutan.adminPanel.hotel.entity.Room.RoomStatus;
import com.goBhutan.adminPanel.hotel.repository.RoomRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class RoomService {

    @Autowired private RoomRepository roomRepo;
    @Autowired private HotelRepository hotelRepo;
    @Autowired private RoomTypeRepository roomTypeRepo;

    public List<Room> getAllRooms() {
        return roomRepo.findAll();
    }

    public Room getRoomById(Long id) {
        return roomRepo.findById(id).orElseThrow(() -> new RuntimeException("Room not found"));
    }

    public Room createRoom(Long hotelId, Long roomTypeId, Room room) {
        Hotel hotel = hotelRepo.findById(hotelId).orElseThrow(() -> new RuntimeException("Hotel not found"));
        RoomType type = roomTypeRepo.findById(roomTypeId).orElseThrow(() -> new RuntimeException("Room type not found"));

        room.setHotel(hotel);
        room.setRoomType(type);
        room.setCreatedAt(LocalDateTime.now());
        room.setUpdatedAt(LocalDateTime.now());
        return roomRepo.save(room);
    }

    public Room updateRoom(Long id, Room updated) {
        Room room = getRoomById(id);
        room.setRoomNumber(updated.getRoomNumber());
        room.setFloor(updated.getFloor());
        room.setBasePrice(updated.getBasePrice());
        room.setMaxOccupancy(updated.getMaxOccupancy());
        room.setStatus(updated.getStatus());
        room.setDescription(updated.getDescription());
        room.setUpdatedAt(LocalDateTime.now());
        return roomRepo.save(room);
    }

    public void deleteRoom(Long id) {
        roomRepo.deleteById(id);
    }
}