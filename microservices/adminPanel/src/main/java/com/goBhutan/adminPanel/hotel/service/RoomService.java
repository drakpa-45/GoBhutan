package com.goBhutan.adminPanel.hotel.service;

import com.goBhutan.adminPanel.common.config.FileUploadProperties;
import com.goBhutan.adminPanel.common.exception.ResourceNotFoundException;
import com.goBhutan.adminPanel.hotel.dto.RoomDTO;
import com.goBhutan.adminPanel.hotel.dto.RoomResponseDTO;
import com.goBhutan.adminPanel.hotel.entity.*;
import com.goBhutan.adminPanel.hotel.mapper.RoomMapper;
import com.goBhutan.adminPanel.hotel.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private AmenityRepository amenityRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private RoomMapper roomMapper;

    @Value("${file.upload-dir:/opt/uploads/}")
    private String uploadDirectory;

    private FileUploadProperties fileUploadProperties;

    @Transactional
    public RoomResponseDTO createRoom(RoomDTO roomDTO, List<MultipartFile> roomImages) throws IOException {
        // Validate hotel exists
        Hotel hotel = hotelRepository.findById(roomDTO.getHotelId())
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id: " + roomDTO.getHotelId()));

        // Check if room number already exists for this hotel
        if (roomRepository.existsByHotelIdAndRoomNumber(hotel.getId(), roomDTO.getRoomNumber())) {
            throw new IllegalArgumentException("Room number " + roomDTO.getRoomNumber() + " already exists for this hotel");
        }

        // Create room entity
        Room room = new Room();
        room.setRoomNumber(roomDTO.getRoomNumber());
        room.setRoomSize(roomDTO.getRoomSize());
        room.setHotel(hotel);
        room.setRoomTypeId(roomDTO.getRoomTypeId());
        room.setFloor(roomDTO.getFloor());
        room.setBasePrice(roomDTO.getBasePrice());
        room.setMaxOccupancy(roomDTO.getMaxOccupancy());
        room.setStatus(roomDTO.getStatus() != null ? roomDTO.getStatus() : Room.RoomStatus.AVAILABLE);
        room.setCurrentCheckInDate(roomDTO.getCurrentCheckInDate());
        room.setCurrentCheckOutDate(roomDTO.getCurrentCheckOutDate());
        room.setDescription(roomDTO.getDescription());
        room.setIsActive(roomDTO.getIsActive() != null ? roomDTO.getIsActive() : true);
        room.setCreatedAt(LocalDateTime.now());
        room.setUpdatedAt(LocalDateTime.now());

        // Handle amenities if provided
        if (roomDTO.getAmenityIds() != null && !roomDTO.getAmenityIds().isEmpty()) {
            List<Amenity> amenities = amenityRepository.findAllById(roomDTO.getAmenityIds());
            room.setAmenities(new HashSet<>(amenities));
        }

        // Save room
        Room savedRoom = roomRepository.save(room);

        // Handle images if provided
        if (roomImages != null && !roomImages.isEmpty()) {
            List<HotelImage> savedImages = saveRoomImages(savedRoom, roomImages);
            savedRoom.setImages(savedImages);
        }

        // Convert to response DTO
        return roomMapper.toRoomResponseDTO(savedRoom);
    }

    @Transactional
    public RoomResponseDTO updateRoom(Long id, RoomDTO roomDTO, List<MultipartFile> roomImages, List<Long> deleteImageIds) throws IOException {
        // Find existing room
        Room existingRoom = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));

        // Validate hotel exists ONLY if hotel ID is provided and being changed
        if (roomDTO.getHotelId() != null && !existingRoom.getHotel().getId().equals(roomDTO.getHotelId())) {
            Hotel hotel = hotelRepository.findById(roomDTO.getHotelId())
                    .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id: " + roomDTO.getHotelId()));
            existingRoom.setHotel(hotel);
        }

        // Check if room number already exists for this hotel (excluding current room)
        if (roomDTO.getRoomNumber() != null &&
                !existingRoom.getRoomNumber().equals(roomDTO.getRoomNumber()) &&
                roomRepository.existsByHotelIdAndRoomNumber(existingRoom.getHotel().getId(), roomDTO.getRoomNumber())) {
            throw new IllegalArgumentException("Room number " + roomDTO.getRoomNumber() + " already exists for this hotel");
        }

        // Update basic fields (only if not null)
        if (roomDTO.getRoomNumber() != null) {
            existingRoom.setRoomNumber(roomDTO.getRoomNumber());
        }
        if (roomDTO.getRoomSize() != null) {
            existingRoom.setRoomSize(roomDTO.getRoomSize());
        }
        if (roomDTO.getFloor() != null) {
            existingRoom.setFloor(roomDTO.getFloor());
        }
        if (roomDTO.getRoomTypeId() != null) {
            existingRoom.setRoomTypeId(roomDTO.getRoomTypeId());
        }
        if (roomDTO.getBasePrice() != null) {
            existingRoom.setBasePrice(roomDTO.getBasePrice());
        }
        if (roomDTO.getMaxOccupancy() != null) {
            existingRoom.setMaxOccupancy(roomDTO.getMaxOccupancy());
        }
        if (roomDTO.getStatus() != null) {
            existingRoom.setStatus(roomDTO.getStatus());
        }
        if (roomDTO.getCurrentCheckInDate() != null) {
            existingRoom.setCurrentCheckInDate(roomDTO.getCurrentCheckInDate());
        }
        if (roomDTO.getCurrentCheckOutDate() != null) {
            existingRoom.setCurrentCheckOutDate(roomDTO.getCurrentCheckOutDate());
        }
        if (roomDTO.getDescription() != null) {
            existingRoom.setDescription(roomDTO.getDescription());
        }
        if (roomDTO.getIsActive() != null) {
            existingRoom.setIsActive(roomDTO.getIsActive());
        }
        existingRoom.setUpdatedAt(LocalDateTime.now());

        // Update amenities if provided
        if (roomDTO.getAmenityIds() != null && !roomDTO.getAmenityIds().isEmpty()) {
            List<Amenity> amenities = amenityRepository.findAllById(roomDTO.getAmenityIds());
            existingRoom.setAmenities(new HashSet<>(amenities));
        }

        // Delete specified images
        if (deleteImageIds != null && !deleteImageIds.isEmpty()) {
            deleteRoomImages(existingRoom, deleteImageIds);
        }

        // Save room
        Room savedRoom = roomRepository.save(existingRoom);

        // Add new images if provided
        if (roomImages != null && !roomImages.isEmpty()) {
            List<HotelImage> newImages = saveRoomImages(savedRoom, roomImages);
            if (savedRoom.getImages() == null) {
                savedRoom.setImages(new ArrayList<>());
            }
            savedRoom.getImages().addAll(newImages);
        }

        // Convert to response DTO
        return roomMapper.toRoomResponseDTO(savedRoom);
    }

    public RoomResponseDTO getRoomById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));
        return roomMapper.toRoomResponseDTO(room);
    }

    public List<RoomResponseDTO> getRoomsByHotelId(Long hotelId) {
        List<Room> rooms = roomRepository.findByHotelId(hotelId);
        return rooms.stream()
                .map(roomMapper::toRoomResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteRoom(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));

        // Delete all images
        if (room.getImages() != null && !room.getImages().isEmpty()) {
            for (HotelImage image : room.getImages()) {
                deleteImageFile(image.getUrl());
            }
        }

        roomRepository.delete(room);
    }

    private List<HotelImage> saveRoomImages(Room room, List<MultipartFile> images) throws IOException {
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

            // Create file path: uploads/rooms/{roomId}/{filename}
            Path roomDir = uploadPath.resolve(String.valueOf(room.getId()));
            if (!Files.exists(roomDir)) {
                Files.createDirectories(roomDir);
            }

            Path filePath = roomDir.resolve(uniqueFilename);

            // Save file to disk
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Create relative path for database storage
            String relativePath = uploadDirectory + "/" + room.getId() + "/" + uniqueFilename;

            // Create and save HotelImage entity
            HotelImage roomImage = new HotelImage();
            roomImage.setRoom(room);
            roomImage.setUrl(relativePath);
            roomImage.setTitle(originalFilename);
            roomImage.setCaption("");
            roomImage.setDisplayOrder(i);
            roomImage.setIsPrimary(i == 0); // First image is primary

            HotelImage savedImage = imageRepository.save(roomImage);
            savedImages.add(savedImage);
        }

        return savedImages;
    }

    /*private List<HotelImage> saveRoomImages(Room room, List<MultipartFile> images) throws IOException {
        // Create upload directory: uploads/room/{roomId}/
       // String uploadDirectory = fileUploadProperties.getDirectory();
        Path uploadPath = Paths.get(uploadDirectory, "room", String.valueOf(room.getId()));

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

            // Create file path: uploads/room/{roomId}/{filename}
            Path filePath = uploadPath.resolve(uniqueFilename);

            // Save file to disk
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Create relative path for database storage (e.g., "uploads/room/1/uuid.jpg")
            String relativePath = uploadDirectory + "room/" + room.getId() + "/" + uniqueFilename;

            // Create and save HotelImage entity
            HotelImage roomImage = new HotelImage();
            roomImage.setRoom(room);
            roomImage.setUrl(relativePath);
            roomImage.setTitle(originalFilename);
            roomImage.setCaption("");
            roomImage.setDisplayOrder(i);
            roomImage.setIsPrimary(i == 0); // First image is primary

            HotelImage savedImage = imageRepository.save(roomImage);
            savedImages.add(savedImage);
        }
        return savedImages;
    }*/

    private void deleteRoomImages(Room room, List<Long> imageIds) {
        for (Long imageId : imageIds) {
            HotelImage image = imageRepository.findById(imageId)
                    .orElseThrow(() -> new ResourceNotFoundException("Image not found with id: " + imageId));

            // Verify the image belongs to this room
            if (!image.getRoom().getId().equals(room.getId())) {
                throw new IllegalArgumentException("Image does not belong to this room");
            }

            // Delete file from disk
            deleteImageFile(image.getUrl());

            // Remove from room's image list
            if (room.getImages() != null) {
                room.getImages().remove(image);
            }

            // Delete from database
            imageRepository.delete(image);
        }
    }

    private void deleteImageFile(String imageUrl) {
        try {
            Path filePath = Paths.get(imageUrl);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            System.err.println("Failed to delete image file: " + imageUrl);
        }
    }
}