package com.goBhutan.adminPanel.hotel.controller;

import com.goBhutan.adminPanel.hotel.entity.RoomType;
import com.goBhutan.adminPanel.hotel.repository.RoomTypeRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/room-types")
public class RoomTypeController {

    @Autowired
    private RoomTypeRepository repo;

    @GetMapping
    @PreAuthorize("hasRole('client_admin')")
    public List<RoomType> getAllTypes() {
        return repo.findAll();
    }

    @PostMapping
    @PreAuthorize("hasRole('client_admin')")
    public RoomType create(@RequestBody @Valid RoomType type) {
        return repo.save(type);
    }
}