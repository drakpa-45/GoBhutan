package com.goBhutan.adminPanel.theater.controller;

import com.goBhutan.adminPanel.theater.dto.TheaterLocationCreateDTO;
import com.goBhutan.adminPanel.theater.dto.TheaterLocationDTO;
import com.goBhutan.adminPanel.theater.service.TheaterLocationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/locations")
@PreAuthorize("hasRole('ADMIN')")
public class TheaterLocationController {

    private final TheaterLocationService theaterLocationService;

    public TheaterLocationController(TheaterLocationService theaterLocationService) {
        this.theaterLocationService = theaterLocationService;
    }

    @GetMapping
    public ResponseEntity<Page<TheaterLocationDTO>> getAllLocations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String dzongkhag) {

        Pageable pageable = PageRequest.of(page, size);

        if (dzongkhag != null && !dzongkhag.trim().isEmpty()) {
            List<TheaterLocationDTO> locations = theaterLocationService.getLocationsByDzongkhag(dzongkhag);
            return ResponseEntity.ok(new PageImpl<>(locations, pageable, locations.size()));
        } else {
            Page<TheaterLocationDTO> locations = theaterLocationService.getAllLocations(pageable);
            return ResponseEntity.ok(locations);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<TheaterLocationDTO> getLocationById(@PathVariable String id) {
        TheaterLocationDTO location = theaterLocationService.getLocationById(id);
        return ResponseEntity.ok(location);
    }

    @PostMapping
    public ResponseEntity<TheaterLocationDTO> createLocation(@RequestBody @Valid TheaterLocationCreateDTO createDTO) {
        TheaterLocationDTO createdLocation = theaterLocationService.createLocation(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdLocation);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TheaterLocationDTO> updateLocation(@PathVariable String id,
                                                             @RequestBody @Valid TheaterLocationCreateDTO updateDTO) {
        TheaterLocationDTO updatedLocation = theaterLocationService.updateLocation(id, updateDTO);
        return ResponseEntity.ok(updatedLocation);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLocation(@PathVariable String id) {
        theaterLocationService.deleteLocation(id);
        return ResponseEntity.noContent().build();
    }
}