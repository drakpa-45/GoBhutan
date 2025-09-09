package com.goBhutan.hotel.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.goBhutan.hotel.entity.Amenity;
import com.goBhutan.hotel.entity.Amenity.AmenityCategory;
import com.goBhutan.hotel.repository.AmenityRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class AmenityService {
    
    @Autowired
    private AmenityRepository amenityRepository;
    
    public List<Amenity> getAllAmenities() {
        return amenityRepository.findAll();
    }
    
    public Map<AmenityCategory, List<Amenity>> getAmenitiesByCategory() {
        return getAllAmenities().stream()
                .collect(Collectors.groupingBy(Amenity::getCategory));
    }
    
    public Amenity saveAmenity(Amenity amenity) {
        return amenityRepository.save(amenity);
    }
    
    public void deleteAmenity(Long id) {
        amenityRepository.deleteById(id);
    }
    
    public List<Amenity> searchAmenities(String query) {
        return amenityRepository.findByNameContainingIgnoreCase(query);
    }
}
