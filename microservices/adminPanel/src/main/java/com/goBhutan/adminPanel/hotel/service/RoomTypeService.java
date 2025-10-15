package com.goBhutan.adminPanel.hotel.service;

import com.goBhutan.adminPanel.hotel.entity.RoomTypeMaster;
import com.goBhutan.adminPanel.hotel.repository.RoomTypeMasterRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class RoomTypeService {

    @Autowired
    private RoomTypeMasterRepository roomTypeMasterRepository;

    // ✅ Fetch all room types
    public List<RoomTypeMaster> getAllRoomTypes() {
        return roomTypeMasterRepository.findAll();
    }

    // ✅ Fetch room types for a specific hotel
    public List<RoomTypeMaster> getAllByAdminUserId() {
        // 🔹 Extract logged-in user ID from Keycloak token
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String adminUserId = jwt.getSubject();
        return roomTypeMasterRepository.findByAdminUserId(adminUserId);
    }

    // ✅ Save or update a room type for a specific hotel
    public RoomTypeMaster saveRoomType(RoomTypeMaster roomType) {
        // 🔹 Extract logged-in user ID from Keycloak token
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        roomType.setAdminUserId(jwt.getSubject());

        return roomTypeMasterRepository.save(roomType);
    }

    // ✅ Delete a room type by ID
    public void deleteRoomType(Long id) {
        roomTypeMasterRepository.deleteById(id);
    }
}
