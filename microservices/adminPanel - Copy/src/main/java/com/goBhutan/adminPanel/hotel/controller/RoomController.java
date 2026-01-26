package com.goBhutan.adminPanel.hotel.controller;

import com.goBhutan.adminPanel.common.dto.ApiResponse;
import com.goBhutan.adminPanel.common.exception.ResourceNotFoundException;
import com.goBhutan.adminPanel.hotel.dto.RoomDTO;
import com.goBhutan.adminPanel.hotel.dto.RoomResponseDTO;
import com.goBhutan.adminPanel.hotel.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/api/rooms")
public class RoomController {

    @Autowired
    private RoomService roomService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<RoomResponseDTO>> createRoom(
            @ModelAttribute @Valid RoomDTO roomDTO,
            @RequestPart(required = false, name = "roomImages") List<MultipartFile> roomImages
    ) {
        try {
            RoomResponseDTO createdRoom = roomService.createRoom(roomDTO, roomImages);

            ApiResponse<RoomResponseDTO> response = ApiResponse.<RoomResponseDTO>builder()
                    .success(true)
                    .message("Room created successfully")
                    .data(createdRoom)
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ResourceNotFoundException e) {
            ApiResponse<RoomResponseDTO> errorResponse = ApiResponse.<RoomResponseDTO>builder()
                    .success(false)
                    .message(e.getMessage())
                    .data(null)
                    .build();

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (IllegalArgumentException e) {
            ApiResponse<RoomResponseDTO> errorResponse = ApiResponse.<RoomResponseDTO>builder()
                    .success(false)
                    .message(e.getMessage())
                    .data(null)
                    .build();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            ApiResponse<RoomResponseDTO> errorResponse = ApiResponse.<RoomResponseDTO>builder()
                    .success(false)
                    .message("Failed to create room: " + e.getMessage())
                    .data(null)
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<RoomResponseDTO>> updateRoom(
            @PathVariable Long id,
            @ModelAttribute @Valid RoomDTO roomDTO,
            @RequestPart(required = false, name = "roomImages") List<MultipartFile> roomImages,
            @RequestParam(required = false, name = "deleteImageIds") List<Long> deleteImageIds
    ) {
        try {
            RoomResponseDTO updatedRoom = roomService.updateRoom(id, roomDTO, roomImages, deleteImageIds);

            ApiResponse<RoomResponseDTO> response = ApiResponse.<RoomResponseDTO>builder()
                    .success(true)
                    .message("Room updated successfully")
                    .data(updatedRoom)
                    .build();

            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            ApiResponse<RoomResponseDTO> errorResponse = ApiResponse.<RoomResponseDTO>builder()
                    .success(false)
                    .message(e.getMessage())
                    .data(null)
                    .build();

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            ApiResponse<RoomResponseDTO> errorResponse = ApiResponse.<RoomResponseDTO>builder()
                    .success(false)
                    .message("Failed to update room: " + e.getMessage())
                    .data(null)
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoomResponseDTO>> getRoomById(@PathVariable Long id) {
        try {
            RoomResponseDTO room = roomService.getRoomById(id);

            ApiResponse<RoomResponseDTO> response = ApiResponse.<RoomResponseDTO>builder()
                    .success(true)
                    .message("Room retrieved successfully")
                    .data(room)
                    .build();

            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            ApiResponse<RoomResponseDTO> errorResponse = ApiResponse.<RoomResponseDTO>builder()
                    .success(false)
                    .message(e.getMessage())
                    .data(null)
                    .build();

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @GetMapping("/hotel/{hotelId}")
    public ResponseEntity<ApiResponse<List<RoomResponseDTO>>> getRoomsByHotelId(@PathVariable Long hotelId) {
        try {
            List<RoomResponseDTO> rooms = roomService.getRoomsByHotelId(hotelId);

            ApiResponse<List<RoomResponseDTO>> response = ApiResponse.<List<RoomResponseDTO>>builder()
                    .success(true)
                    .message("Rooms retrieved successfully")
                    .data(rooms)
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<List<RoomResponseDTO>> errorResponse = ApiResponse.<List<RoomResponseDTO>>builder()
                    .success(false)
                    .message("Failed to retrieve rooms: " + e.getMessage())
                    .data(null)
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRoom(@PathVariable Long id) {
        try {
            roomService.deleteRoom(id);

            ApiResponse<Void> response = ApiResponse.<Void>builder()
                    .success(true)
                    .message("Room deleted successfully")
                    .data(null)
                    .build();

            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            ApiResponse<Void> errorResponse = ApiResponse.<Void>builder()
                    .success(false)
                    .message(e.getMessage())
                    .data(null)
                    .build();

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            ApiResponse<Void> errorResponse = ApiResponse.<Void>builder()
                    .success(false)
                    .message("Failed to delete room: " + e.getMessage())
                    .data(null)
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}