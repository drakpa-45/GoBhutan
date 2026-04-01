package com.goBhutan.adminPanel.hotel.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;


@Entity
@Table(name = "tbl_ht_hotels")
@Getter
@Setter
public class Hotel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Hotel name is required")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Description is required")
    @Column(columnDefinition = "TEXT")
    private String description;

    @NotBlank(message = "Address is required")
    @Column(nullable = false)
    private String address;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "Country is required")
    private String country;

    @NotBlank(message = "Postal code is required")
    private String postalCode;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @NotBlank(message = "Email is required")
    private String email;

    private String website;

    @Column(name = "star_rating")
    private Integer starRating;

    @Column(name = "admin_user_id")
    private String adminUserId;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Room> rooms = new ArrayList<>();

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Amenity> amenities = new ArrayList<>();

    // Add this relationship
    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC, id ASC")
    private List<HotelImage> images = new ArrayList<>();

    // Helper methods for amenities
    public void addAmenity(Amenity amenity) {
        amenities.add(amenity);
        amenity.setHotel(this);
    }

    public void removeAmenity(Amenity amenity) {
        amenities.remove(amenity);
        amenity.setHotel(null);
    }

    public void clearAmenities() {
        amenities.clear();
    }

    // Helper methods for images
    public void addImage(HotelImage image) {
        images.add(image);
        image.setHotel(this);
    }

    public void removeImage(HotelImage image) {
        images.remove(image);
        image.setHotel(null);
    }

    // Helper method to set primary image
    public void setPrimaryImage(HotelImage newPrimaryImage) {
        // Remove primary flag from all images
        for (HotelImage image : images) {
            image.setIsPrimary(false);
        }
        // Set new primary image
        if (newPrimaryImage != null && images.contains(newPrimaryImage)) {
            newPrimaryImage.setIsPrimary(true);
        }
    }
}