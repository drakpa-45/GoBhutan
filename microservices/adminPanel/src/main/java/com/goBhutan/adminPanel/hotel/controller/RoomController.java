package com.goBhutan.adminPanel.hotel.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import com.goBhutan.adminPanel.hotel.entity.Room;
import com.goBhutan.adminPanel.hotel.service.RoomService;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Controller
@RequestMapping("/rooms")
public class RoomController {

    @Autowired private RoomService roomService;

    @GetMapping
    @PreAuthorize("hasRole('client_admin')")
    public List<Room> getAllRooms() {
        return roomService.getAllRooms();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('client_admin')")
    public Room getRoom(@PathVariable Long id) {
        return roomService.getRoomById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('client_admin')")
    public Room createRoom(
            @RequestParam Long hotelId,
            @RequestParam Long roomTypeId,
            @RequestBody @Valid Room room) {
        return roomService.createRoom(hotelId, roomTypeId, room);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('client_admin')")
    public Room updateRoom(@PathVariable Long id, @RequestBody Room room) {
        return roomService.updateRoom(id, room);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('client_admin')")
    public void deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
    }
}