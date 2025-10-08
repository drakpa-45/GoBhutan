package com.goBhutan.adminPanel.hotel.controller;

import com.goBhutan.adminPanel.common.dto.ApiResponse;
import com.goBhutan.adminPanel.hotel.dto.RoomResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import com.goBhutan.adminPanel.hotel.entity.Room;
import com.goBhutan.adminPanel.hotel.service.RoomService;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/rooms")
public class RoomController {

    @Autowired private RoomService roomService;

    @GetMapping("/hotel/{hotel_id}")
    // @PreAuthorize("hasRole('client_admin')")
    public ResponseEntity<ApiResponse<List<RoomResponseDTO>>> getAllRooms(@PathVariable Long hotel_id) {
        List<RoomResponseDTO> rooms = roomService.getAllRooms(hotel_id);

        return ResponseEntity.ok(
                ApiResponse.success("Rooms fetched successfully", rooms)
        );
    }
    @GetMapping("/{id}")
  //  @PreAuthorize("hasRole('client_admin')")
    public Room getRoom(@PathVariable Long id) {
        return roomService.getRoomById(id);
    }

    @PostMapping
  //  @PreAuthorize("hasRole('client_admin')")
    public Room createRoom(
            @RequestParam Long hotelId,
            @RequestParam Long roomTypeId,
            @RequestBody @Valid Room room) {
        return roomService.createRoom(hotelId, roomTypeId, room);
    }

    @PutMapping("/{id}")
// @PreAuthorize("hasRole('client_admin')")
    public ApiResponse<RoomResponseDTO> updateRoom(@PathVariable Long id, @RequestBody Room room) {
        Room updatedRoom = roomService.updateRoom(id, room);

        // Convert the updated Room entity to RoomResponseDTO
        RoomResponseDTO roomDTO = new RoomResponseDTO(updatedRoom);

        // Wrap in ApiResponse
        return ApiResponse.success("Room updated successfully", roomDTO);
    }

    @DeleteMapping("/{id}")
   // @PreAuthorize("hasRole('client_admin')")
    public void deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
    }
}