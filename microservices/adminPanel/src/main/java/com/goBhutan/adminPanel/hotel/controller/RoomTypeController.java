package com.goBhutan.adminPanel.hotel.controller;

import com.goBhutan.adminPanel.common.dto.ApiResponse;
import com.goBhutan.adminPanel.hotel.dto.RoomTypeMasterDTO;
import com.goBhutan.adminPanel.hotel.entity.RoomTypeMaster;
import com.goBhutan.adminPanel.hotel.service.RoomTypeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/api/room-types")
public class RoomTypeController {

    @Autowired
    private RoomTypeService roomTypeService;

    // ✅ Get room types for specific user
    @GetMapping()
    public ApiResponse<List<RoomTypeMasterDTO>> getAllByAdminUserId() {
        List<RoomTypeMasterDTO> dtos = roomTypeService.getAllByAdminUserId()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ApiResponse.success("Fetched room types for user ", dtos);
    }

    // ✅ Create new room type for hotel
    @PostMapping()
    public ApiResponse<RoomTypeMasterDTO> create(
            @RequestBody @Valid RoomTypeMasterDTO roomTypeDTO) {

        // Convert DTO to entity
        RoomTypeMaster roomType = new RoomTypeMaster();
        roomType.setName(roomTypeDTO.name());
        roomType.setDescription(roomTypeDTO.description());

        // Save via service
        RoomTypeMaster saved = roomTypeService.saveRoomType(roomType);

        // Return DTO in ApiResponse
        return ApiResponse.success("Room type created successfully", toDTO(saved));
    }

    // ✅ Update room type for a specific hotel
    @PutMapping("/{id}/roomType")
    public ApiResponse<RoomTypeMasterDTO> update(
            @PathVariable Long id,
            @RequestBody @Valid RoomTypeMasterDTO roomTypeDTO
    ) {
        // Convert DTO to entity
        RoomTypeMaster roomType = new RoomTypeMaster();
        roomType.setId(id);
        roomType.setName(roomTypeDTO.name());
        roomType.setDescription(roomTypeDTO.description());

        // Call service to save/update the RoomType with the hotel association
        RoomTypeMaster updatedRoomType = roomTypeService.saveRoomType(roomType);

        // Return a standard API response
        return ApiResponse.success("Room type updated successfully", toDTO(updatedRoomType));
    }

    // ✅ Delete room type
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        roomTypeService.deleteRoomType(id);
        return ApiResponse.success("Room type deleted successfully", null);
    }

    // Helper: convert entity to DTO
    private RoomTypeMasterDTO toDTO(RoomTypeMaster roomType) {
        return new RoomTypeMasterDTO(
                roomType.getId(),
                roomType.getName(),
                roomType.getDescription()
        );
    }
}
