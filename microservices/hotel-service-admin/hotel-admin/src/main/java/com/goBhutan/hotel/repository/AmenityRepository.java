package com.goBhutan.hotel.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.goBhutan.hotel.entity.Amenity;
import com.goBhutan.hotel.entity.Amenity.AmenityCategory;

@Repository
public interface AmenityRepository extends JpaRepository<Amenity, Long> {
    List<Amenity> findByCategory(AmenityCategory category);
    List<Amenity> findByNameContainingIgnoreCase(String name);
}
