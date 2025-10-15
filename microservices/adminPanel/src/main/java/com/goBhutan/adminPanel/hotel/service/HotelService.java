package com.goBhutan.adminPanel.hotel.service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.goBhutan.adminPanel.hotel.dto.HotelResponseDTO;
import com.goBhutan.adminPanel.hotel.entity.*;
import com.goBhutan.adminPanel.hotel.mapper.HotelMapper;
import com.goBhutan.adminPanel.hotel.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
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
    private  RoomTypeRepository roomTypeRepository;
    @Autowired
    private  AmenityRepository amenityRepository;
    @Autowired
    private ImageRepository imageRepository;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private StorageService storageService;


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

/*    public Hotel createHotel(Hotel hotel) {
        hotel.setCreatedAt(LocalDateTime.now());
        hotel.setUpdatedAt(LocalDateTime.now());
        // 🔹 Extract Keycloak userId from token
        Jwt principal = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = principal.getSubject(); // Keycloak "sub" claim
        hotel.setAdminUserId(userId);
        // Set hotel reference in rooms
        if (hotel.getRooms() != null) {
            for (Room room : hotel.getRooms()) {
                room.setHotel(hotel);
            }
        }
        return hotelRepository.save(hotel);
    }*/

    public Hotel createHotel(Hotel hotel,
                             List<MultipartFile> hotelImages,
                             Map<Long, List<MultipartFile>> roomImagesMap) {
        // 🔹 Extract logged-in user ID from Keycloak token
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        hotel.setAdminUserId(jwt.getSubject());

        // 🔹 Initialize timestamps
        hotel.setCreatedAt(LocalDateTime.now());
        hotel.setUpdatedAt(LocalDateTime.now());

        // 🔹 Handle hotel-level amenities
        if (hotel.getAmenities() != null && !hotel.getAmenities().isEmpty()) {
            Set<Long> amenityIds = hotel.getAmenities().stream()
                    .filter(a -> a.getId() != null)
                    .map(Amenity::getId)
                    .collect(Collectors.toSet());

            if (!amenityIds.isEmpty()) {
                List<Amenity> amenities = amenityRepository.findAllById(amenityIds);
                if (amenities.size() != amenityIds.size()) {
                    throw new RuntimeException("Some hotel amenities were not found");
                }
                hotel.setAmenities(new HashSet<>(amenities));
            }
        }

        // 🔹 Handle rooms and their details
        if (hotel.getRooms() != null && !hotel.getRooms().isEmpty()) {
            for (Room room : hotel.getRooms()) {
                room.setHotel(hotel);

                // Resolve room type safely
                if (room.getRoomType() != null && room.getRoomType().getId() != null) {
                    RoomType roomType = roomTypeRepository.findById(room.getRoomType().getId())
                            .orElseThrow(() -> new RuntimeException(
                                    "Room type not found: " + room.getRoomType().getId()));
                    room.setRoomType(roomType);
                }

                // Resolve room amenities safely
                if (room.getAmenities() != null && !room.getAmenities().isEmpty()) {
                    Set<Long> amenityIds = room.getAmenities().stream()
                            .filter(a -> a.getId() != null)
                            .map(Amenity::getId)
                            .collect(Collectors.toSet());

                    if (!amenityIds.isEmpty()) {
                        List<Amenity> amenities = amenityRepository.findAllById(amenityIds);
                        if (amenities.size() != amenityIds.size()) {
                            throw new RuntimeException("Some room amenities were not found");
                        }
                        room.setAmenities(new HashSet<>(amenities));
                    }
                }
            }
        }

        // 🔹 Persist hotel along with rooms and amenities (cascade = ALL)
        Hotel savedHotel = hotelRepository.save(hotel);

        // 🔹 Handle hotel images
        if (hotelImages != null && !hotelImages.isEmpty()) {
            List<Image> images = hotelImages.stream().map(file -> {
                String url = storageService.upload(file); // S3/local storage logic
                Image image = new Image();
                image.setHotel(savedHotel);
                image.setUrl(url);
                return image;
            }).toList();
            imageRepository.saveAll(images);
            savedHotel.setImages(images);
        }

        // 🔹 Handle room images
        if (roomImagesMap != null && !roomImagesMap.isEmpty()) {
            for (Map.Entry<Long, List<MultipartFile>> entry : roomImagesMap.entrySet()) {
                Long roomId = entry.getKey();
                Room room = roomRepository.findById(roomId)
                        .orElseThrow(() -> new RuntimeException("Room not found for image upload: " + roomId));

                List<Image> roomImages = entry.getValue().stream().map(file -> {
                    String url = storageService.upload(file);
                    Image image = new Image();
                    image.setRoom(room);
                    image.setUrl(url);
                    return image;
                }).toList();
                imageRepository.saveAll(roomImages);
                room.setImages(roomImages);
            }
        }

        return savedHotel;
    }

    public HotelResponseDTO updateHotel(Long id, Hotel updated,
                                        List<MultipartFile> hotelImages,
                                        Map<Long, List<MultipartFile>> roomImagesMap) {

        Hotel hotel = getHotelById(id);

        // -----------------------------
        // 1️⃣ Update basic fields
        // -----------------------------
        hotel.setName(updated.getName());
        hotel.setDescription(updated.getDescription());
        hotel.setAddress(updated.getAddress());
        hotel.setCity(updated.getCity());
        hotel.setState(updated.getState());
        hotel.setCountry(updated.getCountry());
        hotel.setPostalCode(updated.getPostalCode());
        hotel.setPhoneNumber(updated.getPhoneNumber());
        hotel.setEmail(updated.getEmail());
        hotel.setWebsite(updated.getWebsite());
        hotel.setStarRating(updated.getStarRating());
        hotel.setUpdatedAt(LocalDateTime.now());

        // -----------------------------
        // 2️⃣ Update hotel-level amenities
        // -----------------------------
        if (updated.getAmenities() != null) {
            Set<Long> amenityIds = updated.getAmenities().stream()
                    .filter(a -> a.getId() != null)
                    .map(Amenity::getId)
                    .collect(Collectors.toSet());
            if (!amenityIds.isEmpty()) {
                List<Amenity> amenities = amenityRepository.findAllById(amenityIds);
                hotel.setAmenities(new HashSet<>(amenities));
            } else {
                hotel.getAmenities().clear();
            }
        }

        // -----------------------------
        // 3️⃣ Update rooms and their amenities
        // -----------------------------
        if (updated.getRooms() != null) {
            List<Room> updatedRooms = new ArrayList<>();
            for (Room room : updated.getRooms()) {
                Room managedRoom;
                if (room.getId() != null) {
                    // Existing room
                    managedRoom = roomRepository.findById(room.getId())
                            .orElseThrow(() -> new RuntimeException("Room not found: " + room.getId()));
                } else {
                    // New room
                    managedRoom = new Room();
                    managedRoom.setHotel(hotel);
                }

                // Update fields
                managedRoom.setRoomNumber(room.getRoomNumber());
                managedRoom.setFloor(room.getFloor());
                managedRoom.setBasePrice(room.getBasePrice());
                managedRoom.setMaxOccupancy(room.getMaxOccupancy());
                managedRoom.setStatus(room.getStatus());
                managedRoom.setDescription(room.getDescription());
                managedRoom.setIsActive(room.getIsActive());
                managedRoom.setUpdatedAt(LocalDateTime.now());

                // Room type
                if (room.getRoomType() != null && room.getRoomType().getId() != null) {
                    RoomType roomType = roomTypeRepository.findById(room.getRoomType().getId())
                            .orElseThrow(() -> new RuntimeException("Room type not found: " + room.getRoomType().getId()));
                    managedRoom.setRoomType(roomType);
                }

                // Room amenities
                if (room.getAmenities() != null) {
                    Set<Long> roomAmenityIds = room.getAmenities().stream()
                            .filter(a -> a.getId() != null)
                            .map(Amenity::getId)
                            .collect(Collectors.toSet());
                    if (!roomAmenityIds.isEmpty()) {
                        List<Amenity> roomAmenities = amenityRepository.findAllById(roomAmenityIds);
                        managedRoom.setAmenities(new HashSet<>(roomAmenities));
                    } else {
                        managedRoom.getAmenities().clear();
                    }
                }

                updatedRooms.add(managedRoom);
            }

            hotel.setRooms(updatedRooms);
        }

        // -----------------------------
        // 4️⃣ Save hotel first (cascade saves rooms)
        // -----------------------------
        Hotel savedHotel = hotelRepository.save(hotel);

        // -----------------------------
        // 5️⃣ Update hotel images
        // -----------------------------
        if (hotelImages != null && !hotelImages.isEmpty()) {
            // Optionally: delete old images or keep
            // imageRepository.deleteByHotel(savedHotel);

            List<Image> images = hotelImages.stream().map(file -> {
                String url = storageService.upload(file);
                Image image = new Image();
                image.setHotel(savedHotel);
                image.setUrl(url);
                image.setCreatedAt(LocalDateTime.now());
                return image;
            }).toList();

            imageRepository.saveAll(images);
            savedHotel.setImages(images);
        }

        // -----------------------------
        // 6️⃣ Update room images
        // -----------------------------
        if (roomImagesMap != null && !roomImagesMap.isEmpty()) {
            for (Map.Entry<Long, List<MultipartFile>> entry : roomImagesMap.entrySet()) {
                Long roomId = entry.getKey();
                Room room = roomRepository.findById(roomId)
                        .orElseThrow(() -> new RuntimeException("Room not found for image update: " + roomId));

                List<Image> roomImages = entry.getValue().stream().map(file -> {
                    String url = storageService.upload(file);
                    Image image = new Image();
                    image.setRoom(room);
                    image.setUrl(url);
                    image.setCreatedAt(LocalDateTime.now());
                    return image;
                }).toList();

                imageRepository.saveAll(roomImages);
                room.setImages(roomImages);
            }
        }

        return HotelMapper.toHotelResponseDTO(savedHotel);
    }


    public void deleteHotel(Long id) {
        hotelRepository.deleteById(id);
    }
}
