package com.goBhutan.adminPanel.theater.service;

import com.goBhutan.adminPanel.theater.dto.MovieCategoryCreateDTO;
import com.goBhutan.adminPanel.theater.dto.MovieCategoryDTO;
import com.goBhutan.adminPanel.theater.entity.MovieCategory;
import com.goBhutan.adminPanel.theater.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MovieCategoryService {

    private final MovieCategoryRepository movieCategoryRepository;

    public MovieCategoryService(MovieCategoryRepository movieCategoryRepository) {
        this.movieCategoryRepository = movieCategoryRepository;
    }

    public Page<MovieCategoryDTO> getAllCategories(Pageable pageable) {
        return movieCategoryRepository.findAllByIsActiveTrueOrderByCreatedAtDesc(pageable)
                .map(this::convertToDTO);
    }

    public List<MovieCategoryDTO> getAllActiveCategoriesList() {
        return movieCategoryRepository.findByIsActiveTrueOrderByNameAsc()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public MovieCategoryDTO getCategoryById(String id) {
        MovieCategory category = movieCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        return convertToDTO(category);
    }

    public MovieCategoryDTO createCategory(MovieCategoryCreateDTO createDTO) {
        if (movieCategoryRepository.existsByNameIgnoreCase(createDTO.getName())) {
            throw new RuntimeException("Category with name '" + createDTO.getName() + "' already exists");
        }

        MovieCategory category = new MovieCategory();
        category.setName(createDTO.getName());
        category.setDescription(createDTO.getDescription());

        MovieCategory savedCategory = movieCategoryRepository.save(category);
        return convertToDTO(savedCategory);
    }

    public MovieCategoryDTO updateCategory(String id, MovieCategoryCreateDTO updateDTO) {
        MovieCategory category = movieCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

        if (!category.getName().equalsIgnoreCase(updateDTO.getName()) &&
                movieCategoryRepository.existsByNameIgnoreCase(updateDTO.getName())) {
            throw new RuntimeException("Category with name '" + updateDTO.getName() + "' already exists");
        }

        category.setName(updateDTO.getName());
        category.setDescription(updateDTO.getDescription());

        MovieCategory updatedCategory = movieCategoryRepository.save(category);
        return convertToDTO(updatedCategory);
    }

    public void deleteCategory(String id) {
        MovieCategory category = movieCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

        category.setIsActive(false);
        movieCategoryRepository.save(category);
    }

    private MovieCategoryDTO convertToDTO(MovieCategory category) {
        MovieCategoryDTO dto = new MovieCategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setIsActive(category.getIsActive());
        dto.setCreatedAt(category.getCreatedAt().toString());
        return dto;
    }
}