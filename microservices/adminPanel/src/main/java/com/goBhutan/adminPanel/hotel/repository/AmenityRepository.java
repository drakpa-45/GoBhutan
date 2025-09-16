package com.goBhutan.adminPanel.hotel.repository;

import com.goBhutan.adminPanel.hotel.entity.Amenity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AmenityRepository extends JpaRepository<Amenity, Long> {
    List<Amenity> findByCategory(Amenity.AmenityCategory category);
    List<Amenity> findByNameContainingIgnoreCase(String name);
}
