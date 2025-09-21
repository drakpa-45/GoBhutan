//package com.goBhutan.adminPanel.theater.service;
//
//import com.goBhutan.adminPanel.theater.dto.MovieStudioCreateDTO;
//import com.goBhutan.adminPanel.theater.dto.MovieStudioDTO;
//import com.goBhutan.adminPanel.theater.entity.MovieStudio;
//import com.goBhutan.adminPanel.theater.repository.*;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//public class MovieStudioService {
//
//    private final MovieStudioRepository movieStudioRepository;
//
//    public MovieStudioService(MovieStudioRepository movieStudioRepository) {
//        this.movieStudioRepository = movieStudioRepository;
//    }
//
//    public Page<MovieStudioDTO> getAllStudios(Pageable pageable) {
//        return movieStudioRepository.findAllByIsActiveTrueOrderByCreatedAtDesc(pageable)
//                .map(this::convertToDTO);
//    }
//
//    public List<MovieStudioDTO> getAllActiveStudiosList() {
//        return movieStudioRepository.findByIsActiveTrueOrderByNameAsc()
//                .stream()
//                .map(this::convertToDTO)
//                .collect(Collectors.toList());
//    }
//
//    public MovieStudioDTO getStudioById(String id) {
//        MovieStudio studio = movieStudioRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Studio not found with id: " + id));
//        return convertToDTO(studio);
//    }
//
//    public MovieStudioDTO createStudio(MovieStudioCreateDTO createDTO) {
//        if (movieStudioRepository.existsByNameIgnoreCase(createDTO.getName())) {
//            throw new RuntimeException("Studio with name '" + createDTO.getName() + "' already exists");
//        }
//
//        MovieStudio studio = new MovieStudio();
//        studio.setName(createDTO.getName());
//        studio.setDescription(createDTO.getDescription());
//        studio.setCountry(createDTO.getCountry());
//        studio.setWebsite(createDTO.getWebsite());
//
//        MovieStudio savedStudio = movieStudioRepository.save(studio);
//        return convertToDTO(savedStudio);
//    }
//
//    public MovieStudioDTO updateStudio(String id, MovieStudioCreateDTO updateDTO) {
//        MovieStudio studio = movieStudioRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Studio not found with id: " + id));
//
//        if (!studio.getName().equalsIgnoreCase(updateDTO.getName()) &&
//                movieStudioRepository.existsByNameIgnoreCase(updateDTO.getName())) {
//            throw new RuntimeException("Studio with name '" + updateDTO.getName() + "' already exists");
//        }
//
//        studio.setName(updateDTO.getName());
//        studio.setDescription(updateDTO.getDescription());
//        studio.setCountry(updateDTO.getCountry());
//        studio.setWebsite(updateDTO.getWebsite());
//
//        MovieStudio updatedStudio = movieStudioRepository.save(studio);
//        return convertToDTO(updatedStudio);
//    }
//
//    public void deleteStudio(String id) {
//        MovieStudio studio = movieStudioRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Studio not found with id: " + id));
//
//        studio.setIsActive(false);
//        movieStudioRepository.save(studio);
//    }
//
//    private MovieStudioDTO convertToDTO(MovieStudio studio) {
//        MovieStudioDTO dto = new MovieStudioDTO();
//        dto.setId(studio.getId());
//        dto.setName(studio.getName());
//        dto.setDescription(studio.getDescription());
//        dto.setCountry(studio.getCountry());
//        dto.setWebsite(studio.getWebsite());
//        dto.setIsActive(studio.getIsActive());
//        dto.setCreatedAt(studio.getCreatedAt().toString());
//        return dto;
//    }
//}
