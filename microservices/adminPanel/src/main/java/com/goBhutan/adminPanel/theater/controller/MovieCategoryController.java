package com.goBhutan.adminPanel.theater.controller;

import com.goBhutan.adminPanel.theater.dto.MovieCategoryCreateDTO;
import com.goBhutan.adminPanel.theater.dto.MovieCategoryDTO;
import com.goBhutan.adminPanel.theater.service.MovieCategoryService;
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
@RequestMapping("/api/admin/categories")
@PreAuthorize("hasRole('ADMIN')")
public class MovieCategoryController {

    private final MovieCategoryService movieCategoryService;

    public MovieCategoryController(MovieCategoryService movieCategoryService) {
        this.movieCategoryService = movieCategoryService;
    }

    @GetMapping
    public ResponseEntity<Page<MovieCategoryDTO>> getAllCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<MovieCategoryDTO> categories = movieCategoryService.getAllCategories(pageable);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/list")
    public ResponseEntity<List<MovieCategoryDTO>> getAllCategoriesList() {
        List<MovieCategoryDTO> categories = movieCategoryService.getAllActiveCategoriesList();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovieCategoryDTO> getCategoryById(@PathVariable String id) {
        MovieCategoryDTO category = movieCategoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }

    @PostMapping
    public ResponseEntity<MovieCategoryDTO> createCategory(@RequestBody @Valid MovieCategoryCreateDTO createDTO) {
        MovieCategoryDTO createdCategory = movieCategoryService.createCategory(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MovieCategoryDTO> updateCategory(@PathVariable String id,
                                                           @RequestBody @Valid MovieCategoryCreateDTO updateDTO) {
        MovieCategoryDTO updatedCategory = movieCategoryService.updateCategory(id, updateDTO);
        return ResponseEntity.ok(updatedCategory);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable String id) {
        movieCategoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}