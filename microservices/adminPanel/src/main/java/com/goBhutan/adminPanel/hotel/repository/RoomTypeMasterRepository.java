package com.goBhutan.adminPanel.hotel.repository;

import com.goBhutan.adminPanel.hotel.entity.RoomType;
import com.goBhutan.adminPanel.hotel.entity.RoomTypeMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomTypeMasterRepository extends JpaRepository<RoomTypeMaster, Long> {
    List<RoomTypeMaster> findByAdminUserId(String adminUserId);
}
