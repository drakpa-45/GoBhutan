package com.goBhutan.adminPanel.hotel.controller;

import com.goBhutan.adminPanel.hotel.repository.AmenityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.goBhutan.adminPanel.hotel.entity.Amenity;

import jakarta.validation.Valid;

import java.util.List;
@RestController
@RequestMapping("/api/amenities")
public class AmenityController {

    @Autowired
    private AmenityRepository repo;

    @GetMapping
    @PreAuthorize("hasRole('client_admin')")
    public List<Amenity> getAll() {
        return repo.findAll();
    }

    @PostMapping
    @PreAuthorize("hasRole('client_admin')")
    public Amenity create(@RequestBody @Valid Amenity amenity) {
        return repo.save(amenity);
    }
}
