package com.goBhutan.adminPanel.hotel.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.goBhutan.adminPanel.common.config.FileUploadProperties;
import com.goBhutan.adminPanel.common.exception.ResourceNotFoundException;
import com.goBhutan.adminPanel.hotel.dto.AmenityDTO;
import com.goBhutan.adminPanel.hotel.dto.HotelDTO;
import com.goBhutan.adminPanel.hotel.dto.HotelResponseDTO;
import com.goBhutan.adminPanel.hotel.entity.*;
import com.goBhutan.adminPanel.hotel.mapper.HotelMapper;
import com.goBhutan.adminPanel.hotel.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class HotelService {

    @Autowired
    private HotelRepository hotelRepository;
    @Autowired
    private ImageRepository imageRepository;

    private HotelMapper hotelMapper;

    @Value("${file.upload.directory:/opt/uploads/}")
    private String uploadDirectory;

    private FileUploadProperties fileUploadProperties;


    public List<Hotel> getAllHotels() {
        return hotelRepository.findAll();
    }

    public List<HotelResponseDTO> getAllHotelsForCurrentAdmin() {
        // 🔹 Extract Keycloak userId from token
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId;

        if (principal instanceof Jwt jwt) {
            userId = jwt.getSubject(); // Keycloak "sub" claim
        } else if (principal instanceof String str) {
            userId = str; // fallback if principal is String
        } else {
            throw new RuntimeException("Unsupported principal type: " + principal.getClass());
        }

        List<Hotel> hotels = hotelRepository.findByAdminUserId(userId);

        // 🔹 Use HotelMapper to convert full entity graph to DTO
        return hotels.stream()
                .map(HotelMapper::toHotelResponseDTO)
                .collect(Collectors.toList());
    }


    public Hotel getHotelById(Long id) {
        return hotelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hotel not found"));
    }

    @Transactional
    public HotelResponseDTO createHotel(HotelDTO hotelDTO, List<MultipartFile> hotelImages) throws IOException {
        // Create hotel entity from DTO
        Hotel hotel = hotelMapper.toEntity(hotelDTO);
        hotel.setCreatedAt(LocalDateTime.now());
        hotel.setUpdatedAt(LocalDateTime.now());
        hotel.setIsActive(true);

        // Handle amenities if provided
        if (hotelDTO.getAmenities() != null && !hotelDTO.getAmenities().isEmpty()) {
            for (AmenityDTO amenityDTO : hotelDTO.getAmenities()) {
                Amenity amenity = new Amenity();
                amenity.setName(amenityDTO.getName());
                amenity.setDescription(amenityDTO.getDescription());
                amenity.setIconClass(amenityDTO.getIconClass());
                amenity.setCategory(amenityDTO.getCategory());
                hotel.addAmenity(amenity); // Use helper method
            }
        }

        // Save hotel (will cascade save amenities)
        Hotel savedHotel = hotelRepository.save(hotel);

        // Handle images if provided
        if (hotelImages != null && !hotelImages.isEmpty()) {
            for (MultipartFile image : hotelImages) {
                if (!image.isEmpty()) {
                    // Save image logic here
                    // Example: imageService.saveHotelImage(savedHotel.getId(), image);
                    List<HotelImage> savedImages = saveHotelImages(savedHotel, hotelImages);
                    savedHotel.setImages(savedImages);
                }
            }
        }
        // Convert to response DTO
        return hotelMapper.toHotelResponseDTO(savedHotel);
    }


    private List<HotelImage> saveHotelImages(Hotel hotel, List<MultipartFile> images) throws IOException {
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDirectory);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        List<HotelImage> savedImages = new ArrayList<>();

        for (int i = 0; i < images.size(); i++) {
            MultipartFile file = images.get(i);

            // Validate file
            if (file.isEmpty()) {
                continue;
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

            // Create file path: uploads/hotels/{hotelId}/{filename}
            Path hotelDir = uploadPath.resolve(String.valueOf(hotel.getId()));
            if (!Files.exists(hotelDir)) {
                Files.createDirectories(hotelDir);
            }

            Path filePath = hotelDir.resolve(uniqueFilename);

            // Save file to disk
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Create relative path for database storage (e.g., "uploads/hotels/1/uuid.jpg")
            String relativePath = uploadDirectory + "/" + hotel.getId() + "/" + uniqueFilename;

            // Create and save HotelImage entity
            HotelImage hotelImage = new HotelImage();
            hotelImage.setHotel(hotel);
            hotelImage.setUrl(relativePath);
            hotelImage.setTitle(originalFilename);
            hotelImage.setCaption("");
            hotelImage.setDisplayOrder(i);
            hotelImage.setIsPrimary(i == 0); // First image is primary

            HotelImage savedImage = imageRepository.save(hotelImage);
            savedImages.add(savedImage);
        }

        return savedImages;
    }

    /*private List<HotelImage> saveHotelImages(Hotel hotel, List<MultipartFile> images) throws IOException {
        // Create upload directory: uploads/hotel/{hotelId}/
        //String uploadDirectory = fileUploadProperties.getDirectory();
        Path uploadPath = Paths.get(uploadDirectory, "hotel", String.valueOf(hotel.getId()));

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        List<HotelImage> savedImages = new ArrayList<>();

        for (int i = 0; i < images.size(); i++) {
            MultipartFile file = images.get(i);

            // Validate file
            if (file.isEmpty()) {
                continue;
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

            // Create file path: uploads/hotel/{hotelId}/{filename}
            Path filePath = uploadPath.resolve(uniqueFilename);

            // Save file to disk
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Create relative path for database storage (e.g., "uploads/hotel/1/uuid.jpg")
            String relativePath = uploadDirectory + "hotel/" + hotel.getId() + "/" + uniqueFilename;

            // Create and save HotelImage entity
            HotelImage hotelImage = new HotelImage();
            hotelImage.setHotel(hotel);
            hotelImage.setUrl(relativePath);
            hotelImage.setTitle(originalFilename);
            hotelImage.setCaption("");
            hotelImage.setDisplayOrder(i);
            hotelImage.setIsPrimary(i == 0); // First image is primary

            HotelImage savedImage = imageRepository.save(hotelImage);
            savedImages.add(savedImage);
        }

        return savedImages;
    }*/

    @Transactional
    public HotelResponseDTO updateHotel(Long id, HotelDTO hotelDTO, List<MultipartFile> hotelImages, List<Long> deleteImageIds) throws IOException {
        // Find existing hotel
        Hotel existingHotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id: " + id));

        // Update basic fields
        existingHotel.setName(hotelDTO.getName());
        existingHotel.setDescription(hotelDTO.getDescription());
        existingHotel.setAddress(hotelDTO.getAddress());
        existingHotel.setCity(hotelDTO.getCity());
        existingHotel.setState(hotelDTO.getState());
        existingHotel.setCountry(hotelDTO.getCountry());
        existingHotel.setPostalCode(hotelDTO.getPostalCode());
        existingHotel.setPhoneNumber(hotelDTO.getPhoneNumber());
        existingHotel.setEmail(hotelDTO.getEmail());
        existingHotel.setWebsite(hotelDTO.getWebsite());
        existingHotel.setStarRating(hotelDTO.getStarRating());
        existingHotel.setUpdatedAt(LocalDateTime.now());

        // Update admin user ID if provided
        if (hotelDTO.getAdminUserId() != null) {
            existingHotel.setAdminUserId(hotelDTO.getAdminUserId());
        }

        // Update amenities - remove old ones and add new ones
        if (hotelDTO.getAmenities() != null) {
            // Clear existing amenities
            existingHotel.getAmenities().clear();

            // Add new amenities
            for (AmenityDTO amenityDTO : hotelDTO.getAmenities()) {
                if (amenityDTO.getName() == null || amenityDTO.getCategory() == null) {
                    throw new IllegalArgumentException("Amenity name and category are required");
                }

                Amenity amenity = new Amenity();
                amenity.setName(amenityDTO.getName());
                amenity.setDescription(amenityDTO.getDescription());
                amenity.setIconClass(amenityDTO.getIconClass());
                amenity.setCategory(amenityDTO.getCategory());
                existingHotel.addAmenity(amenity);
            }
        }

        // Delete specified images
        if (deleteImageIds != null && !deleteImageIds.isEmpty()) {
            deleteHotelImages(existingHotel, deleteImageIds);
        }

        // Save hotel first to get the ID
        Hotel savedHotel = hotelRepository.save(existingHotel);

        // Add new images if provided
        if (hotelImages != null && !hotelImages.isEmpty()) {
            List<HotelImage> newImages = saveHotelImages(savedHotel, hotelImages);
            savedHotel.getImages().addAll(newImages);
        }

        // Convert to response DTO
        return hotelMapper.toHotelResponseDTO(savedHotel);
    }

    private void deleteHotelImages(Hotel hotel, List<Long> imageIds) {
        for (Long imageId : imageIds) {
            HotelImage image = imageRepository.findById(imageId)
                    .orElseThrow(() -> new ResourceNotFoundException("Image not found with id: " + imageId));

            // Verify the image belongs to this hotel
            if (!image.getHotel().getId().equals(hotel.getId())) {
                throw new IllegalArgumentException("Image does not belong to this hotel");
            }

            // Delete file from disk
            try {
                Path filePath = Paths.get(image.getUrl());
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                // Log the error but continue with database deletion
                System.err.println("Failed to delete image file: " + image.getUrl());
            }

            // Remove from hotel's image list
            hotel.getImages().remove(image);

            // Delete from database
            imageRepository.delete(image);
        }
    }

    public void deleteHotel(Long id) {
        hotelRepository.deleteById(id);
    }

    public void deactivateHotel(Long id) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id: " + id));
        hotel.setIsActive(false);
        hotel.setUpdatedAt(LocalDateTime.now());
        hotelRepository.save(hotel);
    }
}
