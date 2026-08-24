package com.goBhutan.adminPanel.hotel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class HotelDTO {
    @NotBlank(message = "Hotel name is required")
    @Schema(description = "Hotel name")
    private String name;

    @NotBlank(message = "Description is required")
    @Schema(description = "Hotel description")
    private String description;

    @NotBlank(message = "Address is required")
    @Schema(description = "Street address")
    private String address;

    @NotBlank(message = "City is required")
    @Schema(description = "City")
    private String city;

    @NotBlank(message = "State is required")
    @Schema(description = "State/Province")
    private String state;

    @NotBlank(message = "Country is required")
    @Schema(description = "Country")
    private String country;

    @NotBlank(message = "Postal code is required")
    @Schema(description = "Postal code")
    private String postalCode;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    @Schema(description = "Phone number")
    private String phoneNumber;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "Email address")
    private String email;

    @Schema(description = "Website URL")
    private String website;

    @Min(value = 1, message = "Star rating must be between 1 and 5")
    @Max(value = 5, message = "Star rating must be between 1 and 5")
    @Schema(description = "Star rating (1-5)", example = "5")
    private Integer starRating;

    @Schema(description = "Admin user ID from Keycloak")
    private String adminUserId;

    @Schema(description = "List of amenities")
    private List<AmenityDTO> amenities;
}