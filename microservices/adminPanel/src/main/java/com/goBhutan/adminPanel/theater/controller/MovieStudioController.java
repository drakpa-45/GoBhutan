package com.goBhutan.adminPanel.theater.controller;

import com.goBhutan.adminPanel.theater.dto.MovieStudioCreateDTO;
import com.goBhutan.adminPanel.theater.dto.MovieStudioDTO;
import com.goBhutan.adminPanel.theater.service.MovieStudioService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/studios")
@PreAuthorize("hasRole('ADMIN')")
public class MovieStudioController {

    private final MovieStudioService movieStudioService;

    public MovieStudioController(MovieStudioService movieStudioService) {
        this.movieStudioService = movieStudioService;
    }

    @GetMapping
    public ResponseEntity<Page<MovieStudioDTO>> getAllStudios(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<MovieStudioDTO> studios = movieStudioService.getAllStudios(pageable);
        return ResponseEntity.ok(studios);
    }

    @GetMapping("/list")
    public ResponseEntity<List<MovieStudioDTO>> getAllStudiosList() {
        List<MovieStudioDTO> studios = movieStudioService.getAllActiveStudiosList();
        return ResponseEntity.ok(studios);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovieStudioDTO> getStudioById(@PathVariable String id) {
        MovieStudioDTO studio = movieStudioService.getStudioById(id);
        return ResponseEntity.ok(studio);
    }

    @PostMapping
    public ResponseEntity<MovieStudioDTO> createStudio(@RequestBody @Valid MovieStudioCreateDTO createDTO) {
        MovieStudioDTO createdStudio = movieStudioService.createStudio(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdStudio);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MovieStudioDTO> updateStudio(@PathVariable String id,
                                                       @RequestBody @Valid MovieStudioCreateDTO updateDTO) {
        MovieStudioDTO updatedStudio = movieStudioService.updateStudio(id, updateDTO);
        return ResponseEntity.ok(updatedStudio);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudio(@PathVariable String id) {
        movieStudioService.deleteStudio(id);
        return ResponseEntity.noContent().build();
    }
}
